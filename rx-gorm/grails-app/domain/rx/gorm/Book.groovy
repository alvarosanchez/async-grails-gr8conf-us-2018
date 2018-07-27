package rx.gorm

import grails.gorm.rx.mongodb.RxMongoEntity
import org.bson.types.ObjectId

class Book implements RxMongoEntity<Book> {
    ObjectId id
    String title
}
