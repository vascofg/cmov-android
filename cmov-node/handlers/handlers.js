var https = require('https');
var fs = require('fs');
var ursa = require('ursa');

//var privKeyNode = ursa.createPrivateKey(fs.readFileSync('./nodeKeys/privkey.pem'));
var pubkeyAndroid = ursa.createPublicKey(fs.readFileSync('./androidKeys/pubkey.pem'));
var privkeyAndroid = ursa.createPrivateKey(fs.readFileSync('./androidKeys/privkey.pem'));

var ticketPrice = 1;

//var privkeyAndroid = ursa.createPrivateKey(fs.readFileSync('./androidKeys/privkey.pem'));
//var pubkeyNode = ursa.createPublicKey(fs.readFileSync('./nodeKeys/pubkey.pem'));

exports.ticketStatusHandler = function (request, reply) {

    var status = request.payload;
    var ticketModel = request.server.plugins['hapi-sequelized'].db.sequelize.models.Ticket;

    status.forEach(function (ticket) {
        //console.log(ticket.state);

        ticketModel.setTicketUsed(ticketModel, ticket);
    })
};

exports.ticketsTripHandler = function (request, reply) {
    //var pike = request.auth.credentials;
    var tripID = request.params.trip;
    var ticketModel = request.server.plugins['hapi-sequelized'].db.sequelize.models.Ticket;

    if (tripID != null) {
        ticketModel.findAllTicketFromTrip(ticketModel, tripID).then(function (tickets) {

            var ticketsArray = [];
            if (tickets) {
                tickets.forEach(function (ticket) {
                    var ticketsJson = {};
                    ticketsJson.ticket = ticket.ticketEnc;
                    ticketsJson.state = ticket.state;

                    ticketsArray.push(ticketsJson);
                });
            }

            reply(ticketsArray).code(200);
        });
    } else {
        reply("Error in database").code(400);
    }
};

exports.ticketsHandler = function (request, reply) {
    var user = request.auth.credentials;
    var ticketModel = request.server.plugins['hapi-sequelized'].db.sequelize.models.Ticket;

    if (user.email != null) {
        ticketModel.findAllTicketFromUser(ticketModel, user.email).then(function (tickets) {

            var ticketsArray = [];
            if (tickets) {
                tickets.forEach(function (ticket) {
                    var ticketsJson = {};
                    ticketsJson.ticket = ticket.ticketEnc;
                    ticketsJson.state = ticket.state;

                    ticketsArray.push(ticketsJson);
                });
            }

            reply(ticketsArray).code(200);
        });
    } else {
        reply("Error in database").code(400);
    }
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

                var pike = request.payload.pike;

                //console.log(email);
                //console.log(pike);

                var models = request.server.plugins['hapi-sequelized'].db.sequelize.models;

                console.log(pike);
                if (pike !== 'false') {
                    models.Pike.addNewPike(models.Pike, email, name, authToken, expire).then(function (user) {
                            //console.log(user);
                            return reply().code(200);
                        })
                        .catch(function (error) {
                            console.log(error);
                            return reply()
                                .code(400);
                        });
                } else {
                    models.User.addNewUser(models.User, email, name, authToken, expire).then(function (user) {
                            //console.log(user);
                            return reply()
                                .code(200);
                        })
                        .catch(function (error) {
                            console.log(error);
                            return reply()
                                .code(400);
                        });
                }


                //}
            } else if (res.statusCode == 400) {
                return reply().code(403);
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
                            return reply().code(200);
                        }
                        else
                            return reply().code(400);
                    })
                    .catch(function (erro) {
                        //console.log("olha o erro bom");
                        console.log(erro);
                        return reply().code(400);
                    });
            });

        }
    }
};

exports.timetableHandler = function (request, reply) {

    var tripModel = request.server.plugins['hapi-sequelized'].db.sequelize.models.Trip;

    tripModel.findAll().then(function (trips) {
        //console.log(trips);
        //console.log(trips[0].times.A);

        var times = []

        if (trips) {
            trips.forEach(function (trip) {
                //console.log(trip.times);
                var returnTrip = {};
                returnTrip.id = trip.id;
                returnTrip.arrival = trip.arrival;
                returnTrip.departure = trip.departure;
                returnTrip.times = trip.times;

                //var tripsArray = []
                //for (var station in trip.times) {
                //    tripsArray.push({station: station, time: trip.times[station]});
                //}

                //returnTrip.trips = tripsArray;
                times.push(returnTrip);
            });
        }


        return reply(times).code(200);
    });
};

function getTimeByStation(trip, initialStation, finalStation) {
    return trip.filter(
        function (trip) {
            if (trip.station == initialStation || trip.station == finalStation) {
                return trip.station;
            }
            return false;
        }
    );
}


exports.getTicketHandler = function (request, reply) {

    var tripModel = request.server.plugins['hapi-sequelized'].db.sequelize.models.Trip;

    var initialStation = request.payload.initialStation;
    var finalStation = request.payload.finalStation;
    var tripInitialTime = request.payload.tripInitialTime;
    var tripFinalTime = request.payload.tripFinalTime;
    var tripId = request.payload.trip;

    //console.log(initialStation);
    //console.log(finalStation);
    //console.log(tripId);

    var lineA = ['A', 'A1', 'Central', 'B1', 'B'];
    var lineC = ['Central', 'C1', 'C'];

    var indexInitial = lineA.indexOf(initialStation);
    var initialInLineA = true;
    if (indexInitial < 0) {
        indexInitial = lineC.indexOf(initialStation);
        initialInLineA = false;
    }

    var centralIndex = 2;
    var indexFinal = lineA.indexOf(finalStation);
    var finalInLineA = true;
    if (indexFinal < 0) {
        indexFinal = lineC.indexOf(finalStation);
        finalInLineA = false;
    }

    var nStations = 0;
    if ((initialInLineA && finalInLineA) || (!initialInLineA && !finalInLineA)) {
        nStations = Math.abs(indexFinal - indexInitial);
    } else {
        if (initialInLineA) {
            nStations = Math.abs(indexInitial - centralIndex) + indexFinal;
        } else {
            nStations = indexInitial + Math.abs(indexFinal - centralIndex);
        }
    }

    var ticketCost = nStations * ticketPrice;

    var currentTrip = null;
    var currentTripTime = null;
    var currentStationArray = [];
    var secondTrip = null;
    var secondTripTime = null;
    var secondStationArray = [];
    if (tripInitialTime != "" && tripInitialTime != null) {

        if ((initialInLineA && finalInLineA) || (!initialInLineA && !finalInLineA)) {
            tripModel.findTrip(tripModel, initialStation).then(function (trips) {

                trips.forEach(function (trip) {
                    var stationArray = getTimeByStation(trip.times, initialStation, finalStation);
                    //console.log(station);
                    if (stationArray.length == 2) {
                        var firstStation = stationArray[0];
                        if (firstStation.station == initialStation && firstStation.time <= tripInitialTime) {
                            if (currentTrip == null || currentTripTime < firstStation.time) {
                                currentTrip = trip;
                                currentTripTime = firstStation.time;
                                currentStationArray[0] = stationArray[0];
                                currentStationArray[1] = stationArray[1];
                            }
                        }
                    }
                    //console.log(currentTrip);
                });
                createTicket(request, reply, currentTrip, currentStationArray[0], currentStationArray[1], ticketCost);
            });
        } else {
            tripModel.findTrip(tripModel, initialStation).then(function (trips) {

                trips.forEach(function (trip) {
                    var stationArray = getTimeByStation(trip.times, initialStation, "Central");
                    //console.log(station);
                    if (stationArray.length == 2) {
                        var firstStation = stationArray[0];
                        if (firstStation.station == initialStation && firstStation.time <= tripInitialTime) {
                            if (currentTrip == null || currentTripTime < firstStation.time) {
                                currentTrip = trip;
                                currentTripTime = firstStation.time;
                                currentStationArray[0] = stationArray[0];
                                currentStationArray[1] = stationArray[1];
                            }
                        }
                    }
                    //console.log(currentTrip);
                });

                tripModel.findTrip(tripModel, "Central").then(function (trips) {

                    trips.forEach(function (trip) {
                        var stationArray = getTimeByStation(trip.times, "Central", finalStation);
                        //console.log(station);
                        if (stationArray.length == 2) {
                            var firstStation = stationArray[0];
                            if (firstStation.station == "Central" && firstStation.time > currentStationArray[1].time) {
                                if (secondTrip == null || secondTripTime < firstStation.time) {
                                    secondTrip = trip;
                                    secondTripTime = firstStation.time;
                                    secondStationArray[0] = stationArray[0];
                                    secondStationArray[1] = stationArray[1];
                                }
                            }
                        }
                        //console.log(currentTrip);
                    });

                    //console.log(currentTrip);
                    //console.log(secondTrip);
                    //createTicket(request, reply, currentTrip, currentStationArray[0], currentStationArray[1], ticketCost);
                    createMultipleTickets(request, reply, currentTrip, currentStationArray, secondTrip, secondStationArray, ticketCost);
                });
            });
        }
    }
    else if (tripFinalTime != "" && tripFinalTime != null) {
        if ((initialInLineA && finalInLineA) || (!initialInLineA && !finalInLineA)) {
            tripModel.findTrip(tripModel, finalStation).then(function (trips) {

                trips.forEach(function (trip) {
                    var stationArray = getTimeByStation(trip.times, initialStation, finalStation);
                    //console.log(station);

                    if (stationArray.length == 2) {
                        var lastStation = stationArray[1];
                        if (lastStation.station == finalStation && lastStation.time <= tripFinalTime) {
                            if (currentTrip == null || currentTripTime < lastStation.time) {
                                currentTrip = trip;
                                currentTripTime = lastStation.time;
                                currentStationArray[0] = stationArray[0];
                                currentStationArray[1] = stationArray[1];
                            }
                        }
                    }

                    //console.log(currentTrip);
                });

                createTicket(request, reply, currentTrip, currentStationArray[0], currentStationArray[1], ticketCost);
            });
        } else {
            tripModel.findTrip(tripModel, finalStation).then(function (trips) {

                trips.forEach(function (trip) {
                    var stationArray = getTimeByStation(trip.times, "Central", finalStation);
                    //console.log(station);
                    if (stationArray.length == 2) {
                        var lastStation = stationArray[1];
                        if (lastStation.station == finalStation && lastStation.time <= tripFinalTime) {
                            if (currentTrip == null || currentTripTime < lastStation.time) {
                                currentTrip = trip;
                                currentTripTime = lastStation.time;
                                currentStationArray[0] = stationArray[0];
                                currentStationArray[1] = stationArray[1];
                            }
                        }
                    }
                    //console.log(currentTrip);
                });

                tripModel.findTrip(tripModel, "Central").then(function (trips) {

                    trips.forEach(function (trip) {
                        var stationArray = getTimeByStation(trip.times, initialStation, "Central");
                        //console.log(station);
                        if (stationArray.length == 2) {
                            var lastStation = stationArray[1];
                            console.log("olha ele aqui primeiro");
                            if (lastStation.station == "Central" && lastStation.time <= currentStationArray[1].time) {
                                console.log("olha ele aqui");
                                if (secondTrip == null || secondTripTime < lastStation.time) {
                                    secondTrip = trip;
                                    secondTripTime = lastStation.time;
                                    secondStationArray[0] = stationArray[0];
                                    secondStationArray[1] = stationArray[1];
                                }
                            }
                        }
                        //console.log(currentTrip);
                    });

                    //console.log(currentTrip);
                    //console.log(secondTrip);
                    //createTicket(request, reply, currentTrip, currentStationArray[0], currentStationArray[1], ticketCost);

                    createMultipleTickets(request, reply, currentTrip, currentStationArray, secondTrip, secondStationArray, ticketCost);
                });
            });
        }
    }
};


var createMultipleTickets = function (request, reply, currentTrip, currentStationArray, secondTrip, secondStationArray, tripCost) {

    var firstTicket, secondTicket;
    if (currentTrip != null) {

        var ticket = "id:" + currentTrip.id + "--email:" + request.auth.credentials.email + "--firstStation:" + currentStationArray[0].station +
            "--time:" + currentStationArray[0].time + "--lastStation:" + secondStationArray[1].station +
            "--lastTime:" + secondStationArray[1].time + "--price:" + tripCost + "--random:" + Math.random();


        //console.log('Encrypt with Alice Public; Sign with Bob Private');
        firstTicket = pubkeyAndroid.encrypt(ticket, 'utf8', 'base64');
        //var sigTicket = privKeyNode.hashAndSign('sha256', ticket, 'utf8', 'base64');
        //console.log('encrypted', encTicket, '\n');
        //console.log('signed', sig, '\n');
    }

    if (secondTrip != null) {

        var ticket = "id:" + secondTrip.id + "--email:" + request.auth.credentials.email + "--firstStation:" + secondStationArray[0].station +
            "--firstTime:" + secondStationArray[0].time + "--lastStation:" + secondStationArray[1].station +
            "--lastTime:" + secondStationArray[1].time + "--price:" + tripCost + "--random:" + Math.random();


        //console.log('Encrypt with Alice Public; Sign with Bob Private');
        secondTicket = pubkeyAndroid.encrypt(ticket, 'utf8', 'base64');
        //var sigTicket = privKeyNode.hashAndSign('sha256', ticket, 'utf8', 'base64');
        //console.log('encrypted', encTicket, '\n');
        //console.log('signed', sig, '\n');
    }

    reply([
        {
            ticket: firstTicket,
            totalCost: tripCost,
            firstStation: currentStationArray[0],
            lastStation: currentStationArray[1]
        },
        {
            ticket: secondTicket,
            totalCost: tripCost,
            firstStation: secondStationArray[0],
            lastStation: secondStationArray[1]
        }
    ]).code(200);
};


var createTicket = function (request, reply, currentTrip, firstStation, lastStation, tripCost) {

    if (currentTrip != null) {

        var ticket = "id:" + currentTrip.id + "--email:" + request.auth.credentials.email + "--firstStation:" + firstStation.station +
            "--firstTime:" + firstStation.time + "--lastStation:" + lastStation.station +
            "--lastTime:" + lastStation.time + "--price:" + tripCost + "--random:" + Math.random();
        //ticket.trip = currentTrip;
        //ticket.firstStation = firstStation;
        //ticket.lastStation = lastStation;
        //ticket.tripCost = tripCost;


        //console.log('Encrypt with Alice Public; Sign with Bob Private');
        var encTicket = pubkeyAndroid.encrypt(ticket, 'utf8', 'base64');
        //var sigTicket = privKeyNode.hashAndSign('sha256', ticket, 'utf8', 'base64');
        //console.log('encrypted', encTicket, '\n');
        //console.log('signed', sig, '\n');

        //var dTicket = privkeyAndroid.decrypt(encTicket, 'base64', 'utf8');
        //
        //console.log(ticket);
        //console.log(encTicket);
        //console.log(dTicket);

        reply([
            {
                ticket: encTicket,
                totalCost: tripCost,
                firstStation: firstStation,
                lastStation: lastStation
            }
        ]).code(200);
    } else {
        reply().code(400);
    }
};

exports.payHandler = function (request, reply) {
    var user = request.auth.credentials;
    var encryptedTicket = request.payload.ticket;
    var dateSplit = [];

    if (user.cardDate != null)
        dateSplit = user.cardDate.split('/');

    var cardMonth = dateSplit[0];
    var cardYear = dateSplit[1];
    var currentMonth = new Date().getMonth() + 1;
    var currentYear = new Date().getFullYear();
    currentYear = currentYear.toString().substr(2, 2);

    console.log("PAYMENT");
    console.log(currentMonth);
    console.log(currentYear);

    if (user.card != null && ((cardYear > currentYear) || (cardYear == currentYear && cardMonth >= currentMonth))) {

        var ticket = privkeyAndroid.decrypt(encryptedTicket, 'base64', 'utf8');

        console.log(ticket);
        var ticketParams = ticket.split('--');
        var email = ticketParams[1].split(':')[1];
        var tripID = ticketParams[0].split(':')[1];
        console.log(email);
        if (email == request.auth.credentials.email) {
            console.log("valid ticket");

            var models = request.server.plugins['hapi-sequelized'].db.sequelize.models;

            var ticketModel = models.Ticket;

            ticketModel.createTicket(ticketModel, encryptedTicket, email, tripID)._then(function (ticket) {
               //console.log(ticket.state);
               // console.log(ticket.UserEmail);
               // console.log(ticket.TripId);

                if (ticket) {
                    reply().code(200);
                } else {
                    reply("Internal Error").code(400);
                }
            });
        }

    } else {

        reply("Invalid Card Info").code(400);
    }
};


//console.log('Decrypt with Alice Private; Verify with Bob Public');
//var rcv = privkeyAndroid.decrypt(enc, 'base64', 'utf8');
//if (msg !== rcv) {
//    throw new Error("invalid decrypt");
//}
//rcv = new Buffer(rcv).toString('base64');
//if (!pubkeyNode.hashAndVerify('sha256', rcv, sig, 'base64')) {
//    throw new Error("invalid signature");
//}
//console.log('decrypted', msg, '\n');