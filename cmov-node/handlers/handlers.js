var https = require('https');

exports.ticketsHandler = function (request, reply) {
    var user = request.auth.credentials;
    reply(user);
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
            //console.log('BODY: ' + body);
            //console.log(res.statusCode);
            if (res.statusCode == 200) {
                //if (bodyJson.aud == "407408718192.apps.googleusercontent.com") {
                    //if (bodyJson.aud == "286336060185-fnqe7hokq83dbec4oh14vnvqama1aamn.apps.googleusercontent.com") {
                    //console.log("valido");

                    var email = bodyJson.email;
                    var name = bodyJson.name;
                    var authToken = credentials;
                    var expire = bodyJson.exp;

                    var pike = (request.payload.pike.toLowerCase() === "true");

                    //console.log(email);
                    //console.log(pike);

                    var models = request.server.plugins['hapi-sequelized'].db.sequelize.models;

                    if (pike) {
                        models.Pike.addNewPike(models.Pike, email, name, authToken, expire).then(function (user) {
                                //console.log(user);
                                return reply("Ok").code(200);
                            })
                            .catch(function (error) {
                                console.log(error);
                                return reply("Error")
                                    .code(400);
                            });
                    } else {
                        models.User.addNewUser(models.User, email, name, authToken, expire).then(function (user) {
                                //console.log(user);
                                return reply("Ok")
                                    .code(200);
                            })
                            .catch(function (error) {
                                console.log(error);
                                return reply("Error")
                                    .code(400);
                            });
                    }


                //}
            } else if (res.statusCode == 400) {
                return reply("Unauthorized").code(403);
            }
        });
    });
};

exports.updateHandler = function (request, reply) {

    var user = request.auth.credentials;
    var userModel = request.server.plugins['hapi-sequelized'].db.sequelize.models.User;
    var card = request.payload.card,
        cvv = request.payload.cvv,
        date = request.payload.cardDate;
    //    date = new Date("05/16");

    if (user.email) {
        if (card !== "" && card != null && cvv !== "" && cvv !== null) {

            //console.log(card);
            //console.log(cvv);
            //console.log(date.toString());
            userModel.findUserWithEmail(userModel, user.email).then(function (currentUser) {

                userModel.updateCardInfoForUserWithEmail(userModel, currentUser, card, cvv, date).then(function (updatedUser) {
                        //console.log("CENAS BOAS");
                        //console.log(updatedUser);

                        if (updatedUser) {
                            return reply("Ok").code(200);
                        }
                        else
                            return reply("error").code(400);
                    })
                    .catch(function (erro) {
                        //console.log("olha o erro bom");
                        console.log(erro);
                        return reply("Internal error - database").code(400);
                    });
            });

        }
    }
};