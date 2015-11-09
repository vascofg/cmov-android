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
        method: 'POST',
        path: '/ticket',
        handler: function (request, reply) {
            handlers.getTicketHandler(request, reply);
        },
        config: {
            auth: {
                strategy: 'userAuth'
                //scope: 'user' // or [ 'user', 'admin' ]
            }
        }
    },
    {
        method: 'POST',
        path: '/pay',
        handler: function (request, reply) {
            handlers.payHandler(request, reply);
        },
        config: {
            auth: {
                strategy: 'userAuth'
                //scope: 'user' // or [ 'user', 'admin' ]
            }
        }
    },
    {
        method: 'GET',
        path: '/tickets',
        handler: function (request, reply) {
            handlers.ticketsHandler(request, reply);
        },
        config: {
            auth: {
                strategy: 'userAuth'
                //scope: 'user' // or [ 'user', 'admin' ]
            }
        }
    },
    {
        method: 'GET',
        path: '/ticketsTrip/{trip}',
        handler: function (request, reply) {
            handlers.ticketsTripHandler(request, reply);
        },
        config: {
            auth: {
                strategy: 'pikeAuth'
                //scope: 'user' // or [ 'user', 'admin' ]
            }
        }
    },
    {
        method: 'POST',
        path: '/ticketStatus',
        handler: function (request, reply) {
            handlers.ticketStatusHandler(request, reply);
        },
        config: {
            auth: {
                strategy: 'pikeAuth'
                //scope: 'user' // or [ 'user', 'admin' ]
            }
        }
    },
    {
        method: 'GET',
        path: '/timetable',
        handler: function (request, reply) {
            handlers.timetableHandler(request, reply);
        }
    },
    {
        method: 'PATCH',
        path: '/update',
        handler: function (request, reply) {
            handlers.updateHandler(request, reply);
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