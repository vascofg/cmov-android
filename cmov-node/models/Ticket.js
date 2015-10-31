module.exports = function(sequelize, DataTypes) {
    var Ticket = sequelize.define(
        'Ticket',
        {
            id: {
                type: DataTypes.STRING,
                unique: true,
                allowNull: false,
                primaryKey: true
            }
        },
        {
            classMethods: {
                associate: function (models) {
                    Ticket.belongsTo(models.User);
                    Ticket.belongsTo(models.Trip);
                }
            },
            tableName: 'ticket',
            timestamps: false
        }
    );

    //Ticket.belongsTo(User);
    //Ticket.belongsTo(Trip);

    return Ticket;
};