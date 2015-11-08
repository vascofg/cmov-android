var handlers = require('../handlers/handlers.js');

module.exports = [
    {
        method: 'POST',
        path: '/auth',
        handler: function (request, reply) {
            handlers.authHandler(request, reply);
        }
    },
    {
        method: 'GET',
        path: '/tickets',
        handler: function (request, reply) {
            console.log("handler");
            handlers.ticketsHandler(request, reply);
        },
        config: {
            auth: {
                strategy: 'userAuth'
                //scope: 'user' // or [ 'user', 'admin' ]
            }
        }
    }

    //{
    //    method: 'GET',
    //    path: '/{name}',
    //    handler: function (request, reply) {
    //        handlers.handler2(request, reply);
    //    }
    //}
];