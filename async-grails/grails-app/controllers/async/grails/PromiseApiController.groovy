package async.grails

import grails.async.Promise
import grails.async.PromiseList
import groovy.json.JsonSlurper

import static grails.async.web.WebPromises.*

class PromiseApiController {

    def index() {
        log.info "index action: START"

        Promise<Integer> p1 = p1()
        Promise<Integer> p2 = p2()

        List<Integer> results = waitAll p1, p2

        render "Result is: ${results.sum()}"
        log.info "index action: END"
    }

    def nonBlocking() {
        log.info "nonBlocking action: START"

        Promise<Integer> p1 = p1()
        Promise<Integer> p2 = p2()

        Promise renderResult = onComplete([p1, p2]) { List<Integer> results ->
            return results.sum()
        }.then { Integer result ->
            render "Result is: ${result}"
        }
        log.info "nonBlocking action: END"
        return renderResult
    }

    def stock(String ticker) {
        log.info "stock action: START"
        return task {
            ticker = ticker ?: 'GOOG'
            def json = new JsonSlurper().parse(new URL("https://api.iextrading.com/1.0/stock/${ticker}/quote"))
            log.info "stock response received"
            render "ticker: $ticker, price: $json.latestPrice"
        }
    }

    private static Promise<Integer> p1() {
        return task {
            log.info "p1: START"
            sleep 1000
            log.info "p1: END"
            return 1
        }
    }

    private static Promise<Integer> p2() {
        return task {
            log.info "p2: START"
            sleep 2000
            log.info "p2: END"
            return 2
        }
    }


}
