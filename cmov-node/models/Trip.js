module.exports = function (sequelize, DataTypes) {
    var Trip = sequelize.define(
        'Trip',
        {
            id: {
                type: DataTypes.INTEGER,
                unique: true,
                allowNull: false,
                primaryKey: true
            },
            departure: {
                type: DataTypes.STRING,
                allowNull: false
            },
            arrival: {
                type: DataTypes.STRING,
                allowNull: false
            },
            times: {
                type: DataTypes.JSONB,
                allowNull: false
            }
        },
        {
            classMethods: {
                associate: function (models) {
                    Trip.hasMany(models.Ticket);
                },
                findTrip: function (tripModel, initialStation) {
                    return tripModel.find({
                        where: {
                            times: $contains
                        }
                    });
                },
                addTrips: function (tripModel) {
                    tripModel.bulkCreate([
                        {
                            id: 1, departure: 'A', arrival: 'B', times: [
                            {station: 'A', time: '10:00'},
                            {station: 'A1', time: '10:20'},
                            {station: 'Central', time: '11:05'},
                            {station: 'B1', time: '12:15'},
                            {station: 'B', time: '13:30'}
                        ]
                        },
                        {
                            id: 2, departure: 'B', arrival: 'A', times: [
                            {station: 'B', time: '11:30'},
                            {station: 'B1', time: '12:10'},
                            {station: 'Central', time: '12:45'},
                            {station: 'A1', time: ' 13:45'},
                            {station: 'A', time: '15:00'}
                        ]
                        },
                        {
                            id: 3, departure: 'A', arrival: 'B', times: [
                            {station: 'A', time: '13:00'},
                            {station: 'A1', time: '13:20'},
                            {station: 'Central', time: '14:05'},
                            {station: 'B1', time: '15:15'},
                            {station: 'B', time: '16:30'}
                        ]
                        },
                        {
                            id: 4, departure: 'B', arrival: 'A', times: [
                            {station: 'B', time: '14:30'},
                            {station: 'B1', time: '15:10'},
                            {station: 'Central', time: '15:45'},
                            {station: 'A1', time: ' 16:45'},
                            {station: 'A', time: '18:00'}
                        ]
                        },
                        {
                            id: 5, departure: 'A', arrival: 'B', times: [
                            {station: 'A', time: '16:00'},
                            {station: 'A1', time: '16:20'},
                            {station: 'Central', time: '17:05'},
                            {station: 'B1', time: '18:15'},
                            {station: 'B', time: '19:30'}
                        ]
                        },
                        {
                            id: 6, departure: 'B', arrival: 'A', times: [
                            {station: 'B', time: '17:30'},
                            {station: 'B1', time: '18:10'},
                            {station: 'Central', time: '18:45'},
                            {station: 'A1', time: ' 19:45'},
                            {station: 'A', time: '21:00'}
                        ]
                        },
                        {
                            id: 7, departure: 'Central', arrival: 'C', times: [
                            {station: 'Central', time: '07:00'},
                            {station: 'C1', time: '07:30'},
                            {station: 'C', time: '08:00'}
                        ]
                        },
                        {
                            id: 8, departure: 'C', arrival: 'Central', times: [
                            {station: 'C', time: '08:05'},
                            {station: 'C1', time: '08:35'},
                            {station: 'Central', time: '09:05'}
                        ]
                        },
                        {
                            id: 9, departure: 'Central', arrival: 'C', times: [
                            {station: 'Central', time: '11:00'},
                            {station: 'C1', time: '11:30'},
                            {station: 'C', time: '12:00'}
                        ]
                        },
                        {
                            id: 10, departure: 'C', arrival: 'Central', times: [
                            {station: 'C', time: '12:05'},
                            {station: 'C1', time: '12:35'},
                            {station: 'Central', time: '13:05'}
                        ]
                        },
                        {
                            id: 11, departure: 'Central', arrival: 'C', times: [
                            {station: 'Central', time: '17:00'},
                            {station: 'C1', time: '17:30'},
                            {station: 'C', time: '18:00'}
                        ]
                        },
                        {
                            id: 12, departure: 'C', arrival: 'Central', times: [
                            {station: 'C', time: '18:05'},
                            {station: 'C1', time: '18:35'},
                            {station: 'Central', time: '19:05'}
                        ]
                        }
                    ]).then(function () {
                        tripModel.findAll().then(function (trips) {
                            //console.log(trips);
                            console.log(trips[0].times[0].time);
                        });
                    });
                }
            },
            tableName: 'trip',
            timestamps: false
        }
    );
    //
    //Trip.hasMany(Ticket);

    return Trip;
};