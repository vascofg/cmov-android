module.exports = function(sequelize, DataTypes) {
    var Ticket = sequelize.define(
        'Ticket',
        {
            ticketEnc: {
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
                },
                createTicket: function (ticketModel, ticket) {
                    return ticketModel.create({
                        ticketEnc: ticket
                    });
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