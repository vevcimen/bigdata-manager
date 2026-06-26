package io.pivotal.greenplum.spark

import com.typesafe.scalalogging.LazyLogging

import java.sql.{Connection, ResultSet}
import scala.util.{Try, Using}

/** Simple SQL executor wrapping a JDBC Connection. */
class SqlExecutor(connection: Connection) extends LazyLogging {

  def execute(sql: String): Try[Unit] = Try {
    Using.resource(connection.createStatement())(_.execute(sql))
  }

  def executeUpdate(sql: String): Try[Int] = Try {
    Using.resource(connection.createStatement())(_.executeUpdate(sql))
  }

  def executeQuery[Out](sql: String, transformer: ResultSet => Out): Try[Out] = Try {
    Using.resource(connection.createStatement()) { stmt =>
      Using.resource(stmt.executeQuery(sql))(transformer)
    }
  }

  def executeQuery[Out](sql: String, args: Array[Any],
                         transformer: ResultSet => Out): Try[Out] = Try {
    Using.resource(connection.prepareStatement(sql)) { ps =>
      args.zipWithIndex.foreach { case (v, i) =>
        ps.setObject(i + 1, v)
      }
      Using.resource(ps.executeQuery())(transformer)
    }
  }
}
