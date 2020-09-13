package db
package migration
import doobie.implicits._

class V1_0__CreateJokeTable extends DoobieMigration {
  override def migrate =
    sql"""create table joke (
          	id serial not null
          		constraint joke_pk
          		primary key,
          	text text not null,
          	created timestamptz default now() not null
          )""".update.run
}
