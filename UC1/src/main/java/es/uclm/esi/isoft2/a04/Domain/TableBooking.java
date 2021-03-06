package es.uclm.esi.isoft2.a04.Domain;

import java.sql.SQLException;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

/**
 * @version 0.1.2
 *
 */
public class TableBooking {

	/**
	 * Tries to book a table
	 * 
	 * @param date        The day when the table is booked
	 * @param turn        The specific turn within the day
	 * @param guestNumber The number of guests; must be 2, 4, or 6
	 * @param clientID    A string used for identifying the client; might be a name
	 *                    or a code
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws TableNotFoundException Thrown when the system cannot book a table
	 * @throws ParseException
	 * @throws NumberFormatException
	 */
	public void bookTable(Date date, Booking.TURN turn, int guestNumber, String clientID)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException,
			TableNotFoundException, NumberFormatException, ParseException {
		TableImplementation table = findTable(guestNumber, date, turn);
		if ((guestNumber != 2 && guestNumber != 4 && guestNumber != 6) || table == null) {
			throw new TableNotFoundException();
		}

		Booking newBooking = new Booking(table, date, turn);
		newBooking.setClientID(clientID);
		newBooking.create();
		table.setState(Table.RESERVED);
		table.update();
	}

	/**
	 * Returns one free table in that time with the restrictions of the parameter
	 * 
	 * @param seats The number of seats the table must have
	 * @param date  The date in which the table must be free
	 * @param turn  The turn in which the table must be free
	 * @return A free table, or null if no table was found
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ParseException
	 * @throws NumberFormatException
	 */
	public TableImplementation findTable(int seats, Date date, Booking.TURN turn) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException, NumberFormatException, ParseException {
		Booking[] bookings = (new Booking()).readAll();
		Table[] tables = (new TableImplementation()).readAll();
		TableImplementation foundTable = null;
		int i = 0, j;
		boolean isValid;
		while (foundTable == null && i < tables.length) {
			if (tables[i].getSeats() != seats) {
				i++;
				continue;
			}
			if (tables[i].getState() != Table.FREE) {
				i++;
				continue;
			}
			isValid = true;
			for (j = 0; j < bookings.length && isValid; j++) {
				isValid = !(bookings[j].getTable().getID() == tables[i].getID()
						&& bookings[j].getDate().equals(date)
						&& bookings[j].getTurn() == turn);
			}
			if (isValid) {
				foundTable = (TableImplementation) tables[i];
			}
			i++;
		}
		return foundTable;
	}

	/**
	 * @param table The table to be assigned
	 * @return An instance of the WaiterImplementation that has been assigned to
	 *         table
	 * @throws NumberFormatException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws ParseException
	 * @throws InvalidStateException
	 */
	public WaiterImplementation assignWaiter(TableImplementation table)
			throws NumberFormatException, InstantiationException, IllegalAccessException, ClassNotFoundException,
			SQLException, ParseException, InvalidStateException {
		if (table.getState() != Table.RESERVED)
			throw new InvalidStateException();
		WaiterImplementation waiter = new WaiterImplementation();
		waiter = Arrays.stream((WaiterImplementation[]) waiter.readAll()) // Get the waiter with less assigned tables
				.reduce((w1, w2) -> w1.getAssignedTables().size() < w2.getAssignedTables().size() ? w1 : w2).get();

		waiter.assignTable(table);
		waiter.update();
		table.setState(Table.BUSY);
		table.update();
		return waiter;
	}

	/**
	 * This method must be called after 20 minutes of the start of the turn of
	 * booking
	 * 
	 * @param booking The booking to be cancelled
	 * @throws InsuficientTimeElapsedException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws ParseException
	 * @throws NumberFormatException
	 * @throws InvalidStateException
	 */
	public void cancelBooking(Booking booking)
			throws InsuficientTimeElapsedException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, NumberFormatException, ParseException, InvalidStateException {
		if (booking.getTable().getState() != Table.RESERVED)
			throw new InvalidStateException();
		if (((new Date()).getTime() - booking.getDate().getTime()) < 20 * 60 * 1000)
			throw new InsuficientTimeElapsedException();
		booking.delete();
		booking.getTable().setState(Table.FREE);
		booking.getTable().update();
	}
}