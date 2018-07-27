package async.grails

import grails.async.web.AsyncController

import javax.servlet.AsyncContext

class ServletApiController implements AsyncController {

    def index() {
        log.info "index action: START"
        final AsyncContext ctx = startAsync()
        ctx.start {
            log.info "Async context: START"
            sleep 1000
            render 'Done'
            ctx.complete()
            log.info "Async context: END"
        }
        log.info "index action: END"
    }

    def view() {
        log.info "view action: START"
        final AsyncContext ctx = startAsync()
        ctx.start {
            log.info "Async context: START"
            sleep 1000
            render view: "view"
            ctx.dispatch()
            log.info "Async context: END"
        }
        log.info "view action: END"
    }
}
