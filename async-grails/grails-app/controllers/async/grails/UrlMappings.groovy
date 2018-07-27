package async.grails

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "/sse"(view:"/sse")
        "/observable"(view:"/observable")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
