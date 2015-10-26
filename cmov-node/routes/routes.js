var handlers = require('../handlers/handlers.js');

module.exports = [
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