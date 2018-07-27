package rx.gorm

import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

class BootStrap {

    def init = { servletContext ->

        new Book(title: "The Stand").save(flush:true)
                .map { Book savedBook ->
            println savedBook
        }.onErrorReturn { Throwable e ->
            if(e instanceof ValidationException) {
                log.error e.errors
            }
            else {
                log.error("Error saving entity: $e.message", e)
                return INTERNAL_SERVER_ERROR
            }
        }.subscribe()

    }
    def destroy = {
    }
}
