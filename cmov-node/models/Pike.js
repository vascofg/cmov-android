//var Ticket = require('Ticket.js');
module.exports = function(sequelize, DataTypes) {
    var Pike = sequelize.define(
        'Pike',
        {
            email: {
                type: DataTypes.STRING,
                unique: true,
                allowNull: false,
                primaryKey: true
            },
            name: {
                type: DataTypes.STRING
            },
            picture: {
                type: DataTypes.STRING
            }
        },
        {
            classMethods: {
                associate: function (models) {
                },
                addNewPike: function(pikeModel, email) {
                    return pikeModel.create({
                        email: email
                    });
                }
            },
            tableName: 'pike',
            timestamps: false
        }
    );

    //User.hasMany(Ticket);

    return Pike;
};