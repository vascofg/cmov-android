var https = require('https');

exports.handler1 = function (request, reply) {
    reply('Hello, world!');
};

exports.handler2 = function (request, reply) {
    reply('Hello, ' + encodeURIComponent(request.params.name) + '!');
};

exports.authHandler = function (request, reply) {
    var credentials = request.payload.googleCredentials;

    var url = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=" + credentials;
    https.get(url, function (res) {
        //console.log(res.statusCode);
        var body = '';
        res.on('data', function (chunk) {
            body += chunk;
        });
        res.on('end', function () {

            var bodyJson = JSON.parse(body);
            console.log('BODY: ' + body);
            console.log(res.statusCode);
            if (res.statusCode == 200) {
                if (bodyJson.aud == "407408718192.apps.googleusercontent.com") {
                    //console.log("valido");

                    var email = bodyJson.email;

                    var pike = (request.payload.pike.toLowerCase() === "true");

                    console.log(email);
                    console.log(pike);

                    var models = request.server.plugins['hapi-sequelized'].db.sequelize.models;

                    if (pike) {
                        models.Pike.addNewPike(models.Pike, email).then(function (user) {
                                console.log(user);
                                return reply("Ok").code(200);
                            })
                            .catch(function (error) {
                                console.log(error);
                                return reply("Error")
                                    .code(400);
                            });
                    } else {
                        models.User.addNewUser(models.User, email).then(function (user) {
                                console.log(user);
                                return reply("Ok")
                                    .code(200);
                            })
                            .catch(function (error) {
                                console.log(error);
                                return reply("Error")
                                    .code(400);
                            });
                    }


                }
            } else if (res.statusCode == 400) {
                return reply("Unauthorized").code(403);
            }
        });
    });


};