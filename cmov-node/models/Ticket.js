module.exports = function(sequelize, DataTypes) {
    var Ticket = sequelize.define(
        'Ticket',
        {
            ticketEnc: {
                type: DataTypes.TEXT,
                unique: true,
                allowNull: false,
                primaryKey: true
            },
            state: {
                type: DataTypes.TEXT,
                allowNull: false
            }
        },
        {
            classMethods: {
                associate: function (models) {
                    Ticket.belongsTo(models.User);
                    Ticket.belongsTo(models.Trip);
                },
                createTicket: function (ticketModel, ticket, email, tripID) {
                    return ticketModel.create({
                        ticketEnc: ticket,
                        UserEmail: email,
                        TripId: tripID,
                        state: "not used"
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