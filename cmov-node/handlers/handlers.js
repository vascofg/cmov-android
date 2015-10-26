exports.handler1 = function (request, reply) {
    reply('Hello, world!');
};

exports.handler2 = function (request, reply) {
    reply('Hello, ' + encodeURIComponent(request.params.name) + '!');
};