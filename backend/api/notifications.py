"""
Bildirim API'si.

GET    /api/notifications           → Tüm bildirimler (okunmamışlar önce)
GET    /api/notifications/unread    → Sayı
PATCH  /api/notifications/{id}/read → Okundu işaretle
PATCH  /api/notifications/read-all  → Hepsini okundu işaretle
DELETE /api/notifications/{id}      → Sil
"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from ..db.models import Notification
from .deps import get_db

router = APIRouter(prefix="/api/notifications", tags=["notifications"])


@router.get("")
def list_notifications(
    limit: int = 50,
    db: Session = Depends(get_db)
):
    rows = (
        db.query(Notification)
        .order_by(Notification.is_read.asc(), Notification.created_at.desc())
        .limit(limit)
        .all()
    )
    return [_to_dict(n) for n in rows]


@router.get("/unread")
def unread_count(db: Session = Depends(get_db)):
    count = db.query(Notification).filter_by(is_read=False).count()
    return {"count": count}


@router.patch("/{notification_id}/read")
def mark_read(notification_id: int, db: Session = Depends(get_db)):
    n = db.query(Notification).filter_by(id=notification_id).first()
    if not n:
        raise HTTPException(status_code=404, detail="Bildirim bulunamadı")
    n.is_read = True
    db.commit()
    return {"status": "ok"}


@router.patch("/read-all")
def mark_all_read(db: Session = Depends(get_db)):
    db.query(Notification).filter_by(is_read=False).update({"is_read": True})
    db.commit()
    return {"status": "ok"}


@router.delete("/{notification_id}")
def delete_notification(notification_id: int, db: Session = Depends(get_db)):
    n = db.query(Notification).filter_by(id=notification_id).first()
    if not n:
        raise HTTPException(status_code=404, detail="Bildirim bulunamadı")
    db.delete(n)
    db.commit()
    return {"status": "ok"}


def _to_dict(n: Notification) -> dict:
    return {
        "id":         n.id,
        "title":      n.title,
        "message":    n.message,
        "level":      n.level,
        "source":     n.source,
        "is_read":    n.is_read,
        "created_at": n.created_at.isoformat() if n.created_at else None,
    }
