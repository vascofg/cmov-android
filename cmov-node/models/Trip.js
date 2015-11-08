module.exports = function(sequelize, DataTypes) {
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
                type: DataTypes.JSON,
                allowNull: false
            }
        },
        {
            classMethods: {
                associate: function (models) {
                    Trip.hasMany(models.Ticket);
                },
                addTrips: function (tripModel) {
                    tripModel.bulkCreate([
                        {id:1,departure:'A', arrival:'B', times:{
                            A: '10:00',
                            A1: '10:20',
                            Central: '11:05',
                            B1: '12:15',
                            B: '13:30'
                        }},
                        {id:2,departure:'B', arrival:'A', times:{
                            B: '11:30',
                            B1: '12:10',
                            Central: '12:45',
                            A1: ' 13:45',
                            A: '15:00'
                        }},
                        {id:3,departure:'A', arrival:'B', times:{
                            A: '13:00',
                            A1: '13:20',
                            Central: '14:05',
                            B1: '15:15',
                            B: '16:30'
                        }},
                        {id:4,departure:'B', arrival:'A', times:{
                            B: '14:30',
                            B1: '15:10',
                            Central: '15:45',
                            A1: ' 16:45',
                            A: '18:00'
                        }},
                        {id:5,departure:'A', arrival:'B', times:{
                            A: '16:00',
                            A1: '16:20',
                            Central: '17:05',
                            B1: '18:15',
                            B: '19:30'
                        }},
                        {id:6,departure:'B', arrival:'A', times:{
                            B: '17:30',
                            B1: '18:10',
                            Central: '18:45',
                            A1: ' 19:45',
                            A: '21:00'
                        }},
                        {id:7,departure:'Central', arrival:'C', times:{
                            Central: '07:00',
                            C1: '07:30',
                            C: '08:00'
                        }},
                        {id:8,departure:'C', arrival:'Central', times:{
                            C: '08:05',
                            C1: '08:35',
                            Central: '09:05'
                        }},
                        {id:9,departure:'Central', arrival:'C', times:{
                            Central: '11:00',
                            C1: '11:30',
                            C: '12:00'
                        }},
                        {id:10,departure:'C', arrival:'Central', times:{
                            C: '12:05',
                            C1: '12:35',
                            Central: '13:05'
                        }},
                        {id:11,departure:'Central', arrival:'C', times:{
                            Central: '17:00',
                            C1: '17:30',
                            C: '18:00'
                        }},
                        {id:12,departure:'C', arrival:'Central', times:{
                            C: '18:05',
                            C1: '18:35',
                            Central: '19:05'
                        }}
                    ]).then(function() {
                        tripModel.findAll().then(function(trips) {
                            //console.log(trips);
                            console.log(trips[0].times.A);
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