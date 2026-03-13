"""
Kimlik doğrulama ve kullanıcı yönetimi API'si.

POST   /api/auth/login              → Giriş yap, JWT token döndür
GET    /api/auth/me                 → Aktif kullanıcı bilgisi
GET    /api/auth/users              → Tüm kullanıcılar
POST   /api/auth/users              → Yeni kullanıcı oluştur
PUT    /api/auth/users/{user_id}    → Kullanıcı güncelle (şifre dahil)
DELETE /api/auth/users/{user_id}    → Kullanıcı sil
"""
import logging
from datetime import datetime, timedelta
from typing import Optional

import jwt
from fastapi import APIRouter, Depends, HTTPException, Request
from passlib.hash import bcrypt
from pydantic import BaseModel
from sqlalchemy.orm import Session

from ..db.models import User
from .deps import get_db

log = logging.getLogger(__name__)

router = APIRouter(tags=["auth"])

JWT_SECRET = "kasirga-secret-key-change-in-production"
JWT_ALGORITHM = "HS256"
JWT_EXPIRE_HOURS = 24


# ─── Pydantic modeller ────────────────────────────────────────────────────────

class LoginRequest(BaseModel):
    username: str
    password: str

class UserCreate(BaseModel):
    username: str
    password: str
    role: Optional[str] = "admin"

class UserUpdate(BaseModel):
    username: Optional[str] = None
    password: Optional[str] = None
    role: Optional[str] = None


# ─── JWT yardımcıları ─────────────────────────────────────────────────────────

def _create_token(user_id: int, username: str, role: str) -> str:
    payload = {
        "sub": user_id,
        "username": username,
        "role": role,
        "exp": datetime.utcnow() + timedelta(hours=JWT_EXPIRE_HOURS),
    }
    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)


def get_current_user(request: Request, db: Session = Depends(get_db)) -> User:
    """Authorization header'dan JWT token'ı çözümle ve kullanıcıyı döndür."""
    auth = request.headers.get("Authorization", "")
    if not auth.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Token gerekli")
    token = auth[7:]
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token süresi doldu")
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=401, detail="Geçersiz token")
    user = db.query(User).filter_by(id=payload["sub"]).first()
    if not user:
        raise HTTPException(status_code=401, detail="Kullanıcı bulunamadı")
    return user


# ─── Endpoints ─────────────────────────────────────────────────────────────────

@router.post("/api/auth/login")
def login(body: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter_by(username=body.username).first()
    if not user or not bcrypt.verify(body.password, user.password_hash):
        raise HTTPException(status_code=401, detail="Geçersiz kullanıcı adı veya şifre")
    token = _create_token(user.id, user.username, user.role)
    log.info(f"Kullanıcı giriş yaptı: {user.username}")
    return {
        "token": token,
        "user": {"id": user.id, "username": user.username, "role": user.role},
    }


@router.get("/api/auth/me")
def me(current_user: User = Depends(get_current_user)):
    return {
        "id": current_user.id,
        "username": current_user.username,
        "role": current_user.role,
    }


@router.get("/api/auth/users")
def list_users(db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    users = db.query(User).all()
    return [
        {
            "id": u.id,
            "username": u.username,
            "role": u.role,
            "created_at": u.created_at.isoformat() if u.created_at else None,
        }
        for u in users
    ]


@router.post("/api/auth/users", status_code=201)
def create_user(body: UserCreate, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    existing = db.query(User).filter_by(username=body.username).first()
    if existing:
        raise HTTPException(status_code=409, detail=f"Bu kullanıcı zaten mevcut: {body.username}")
    user = User(
        username=body.username,
        password_hash=bcrypt.hash(body.password),
        role=body.role or "admin",
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    log.info(f"Yeni kullanıcı oluşturuldu: {user.username}")
    return {"id": user.id, "username": user.username, "role": user.role}


@router.put("/api/auth/users/{user_id}")
def update_user(user_id: int, body: UserUpdate, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    user = db.query(User).filter_by(id=user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı")
    if body.username is not None:
        dup = db.query(User).filter(User.username == body.username, User.id != user_id).first()
        if dup:
            raise HTTPException(status_code=409, detail=f"Bu kullanıcı adı zaten alınmış: {body.username}")
        user.username = body.username
    if body.password is not None:
        user.password_hash = bcrypt.hash(body.password)
    if body.role is not None:
        user.role = body.role
    db.commit()
    db.refresh(user)
    log.info(f"Kullanıcı güncellendi: {user.username}")
    return {"id": user.id, "username": user.username, "role": user.role}


@router.delete("/api/auth/users/{user_id}")
def delete_user(user_id: int, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    user = db.query(User).filter_by(id=user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı")
    if user.id == current_user.id:
        raise HTTPException(status_code=400, detail="Kendinizi silemezsiniz")
    db.delete(user)
    db.commit()
    log.info(f"Kullanıcı silindi: {user.username}")
    return {"status": "deleted", "id": user_id}
