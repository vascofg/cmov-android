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
        path: '/',
        handler: function (request, reply) {
            handlers.handler1(request, reply);
        }
    },

    {
        method: 'GET',
        path: '/{name}',
        handler: function (request, reply) {
            handlers.handler2(request, reply);
        }
    }
];