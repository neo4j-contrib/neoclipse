package net.sourceforge.sqlexplorer;

import java.sql.SQLException;

import net.sourceforge.sqlexplorer.dbproduct.User;

public class SQLCannotConnectException extends SQLException {
	
	private static final long serialVersionUID = 1L;
	private Throwable cause;
	private User user;

	public SQLCannotConnectException(User user) {
		super(getDesc(user));
		this.user = user;
	}

	public SQLCannotConnectException(User user, Throwable cause) {
		super(getDesc(user) + "\n" + cause.getMessage());
		this.user = user;
		this.cause = cause;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Returns a description of the alias 
	 * @param user
	 * @return
	 */
	private static String getDesc(User user) {
		String result = "Cannot connect to ";
		if (user != null) {
			if (user.getAlias() != null)
				result += user.getAlias().getName() + "/" + user.getUserName();
			else
				result += "user " + user.getUserName();
		} else 
			result += "(no user)";
		return result + ".  Check your URL";
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getCause()
	 */
	@Override
	public Throwable getCause() {
		return cause;
	}
}
