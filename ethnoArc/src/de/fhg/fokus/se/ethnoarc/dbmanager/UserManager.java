/**
*
* Copyright (C) 2006-2008 FhG Fokus
*
* This file is part of the ethnoArc toolkit - a set of programs aimed
* at providing database tools and services for ethnological archives.
*
* You can redistribute the ethnoArc tools and/or modify it
* under the terms of the GNU General Public License Version 3 as published by
* the Free Software Foundation.
*
* For a license to use the ethnoArc tools software under conditions
* other than those described here, or to purchase support for this
* software, please contact Fraunhofer FOKUS by e-mail at the following
* addresses:
*   support@ethnoArc.org
*
* The ethnoArc toolkit is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see <http://www.gnu.org/licenses/>
* or write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*
*/
package de.fhg.fokus.se.ethnoarc.dbmanager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import de.fhg.fokus.se.ethnoarc.common.DBException;
import de.fhg.fokus.se.ethnoarc.common.DBSqlHandler;
import de.fhg.fokus.se.ethnoarc.dbmanager.AppConstants.UserLevels;
/**
 * $Id: UserManager.java,v 1.3 2008/07/01 12:08:52 fchristian Exp $ 
 * @author fokus
 */
public class UserManager {
	/** The logger */
	static Logger logger = Logger.getLogger(UserManager.class.getName());
	/** Algorith to encrypt password. */
	private static final String algorithm = "SHA-256";
	/** The name of the table with user details. */
	private final String userTableName="_dbmappuser";
	/** Username and password of the current user. */
	private String username,pwd;
	/** 
	 * User level of the current user.
	 * @see UserLevels
	 */
	private UserLevels userLevel;

	/** If the pwd of the user is valid. */
	private boolean isValid;
	
	/**
	 * Constructor.
	 * @param username The username of the user.
	 * @param pwd The password of the user.
	 */
	public UserManager(String username,String pwd)
	{
		this.username=username;
		this.pwd=pwd;
	}

	/**
	 * Verifies if the password is valid.
	 * @return <code>true</code>: The username and the password is valid. <br>
	 * <code>false</code>: Username or password invalid.
	 * @throws DBException :
	 * <br><code>DB_CONNECTION_ACCESS_DENIED</code>: Cannot connect to the DB.
	 * <br><code>DB_LOGIN_INVALID</code>: The username of the password is invalid.
	 */
	public boolean pwdIsValid() throws DBException
	{
		DBSqlHandler sqlcon;
		try {
			sqlcon = DBSqlHandler.getInstance();
		} catch (DBException e1) {
			throw new DBException (DBException.DB_CONNECTION_ACCESS_DENIED,"Error connecting to DB.");
		}
		//if the connection is established then send sql statement.
		String sql="SELECT pwd FROM "+userTableName+" WHERE username='"+username+"'";
		String dbpwd="";
		try{
			logger.debug("Get user password in DB");
			dbpwd=sqlcon.executeGetValue(sql);
			// IF password not found
			if(dbpwd==null)
				throw new DBException(DBException.DB_LOGIN_INVALID,"Application username not valid.");
			//	PASSWORD FOUND	
			else
			{
				try {
					logger.debug("Validating password");
					isValid=verifyPassword(pwd, dbpwd);
				} catch (DBException e) {
					throw new DBException (DBException.APPLICATION_ERROR,"Passport verifier error.");
				}
				if(isValid)
				{
					logger.debug("User is valid... get user level.");
					sql ="SELECT userlevel FROM _dbmappuser WHERE username='"+username+"'";
					String dbuserlevel="";
					try {
						dbuserlevel = DBSqlHandler.getInstance().executeGetValue(sql);
					} catch (Exception e) {
						throw new DBException (DBException.SQL_EXCEPTION,"Error getting user level.");
					}
					userLevel=UserLevels.valueOf(dbuserlevel);
				}
			}
		}catch(Exception e)
		{
			// ERROR GETTING PASSWORD
			logger.warn("Error getting user password from db... checking if user table exist.");
			if(!userTableExist())
			{
				throw new DBException(DBException.DB_LOGIN_INVALID,"User table not found.");
			}
			//user table exists but user name does not exist
			else
			{
				throw new DBException(DBException.DB_LOGIN_INVALID,"Application username not valid.");
			}
		}

		return isValid;
	}
	public UserLevels getUserLevel()
	{
		return userLevel;
	}

	public static String hashPassword(String password) 
	throws DBException {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			byte[] hashBytes = digest.digest(password.getBytes());
			//return new String(hashBytes,"utf-8");
			return Base64.encode(hashBytes);
			/*} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			throw new DBException(DBException.APPLICATION_ERROR,"Error encoding password.");*/
		}catch (NoSuchAlgorithmException e)
		{
			throw new DBException(DBException.APPLICATION_ERROR,"Error encoding password:NoSuchAlgorithmException");
		}
	}

	private boolean verifyPassword(String password, String hashString) 
	throws DBException {
		//System.out.println(password+" ::: "+hashString+":"+hashPassword(password)+":"+hashString.length()+"_"+hashPassword(password).length());
		String passwordH = hashPassword(password);
		if(passwordH.length()<hashString.length())
			return hashString.startsWith(hashPassword(password));
		else
			return hashPassword(password).startsWith(hashString);
	}

	//-------------- User table in MySql ----
	/**
	 * Checks if the user table exists in the DB.
	 */
	private boolean userTableExist() throws DBException
	{
		boolean exist=false;
		DBSqlHandler sqlcon;
		try {
			sqlcon = DBSqlHandler.getInstance();
		} catch (DBException e1) {
			throw new DBException (DBException.DB_CONNECTION_ACCESS_DENIED,"Error check if table exist.");
		}
		String sql = "SHOW TABLES LIKE '"+userTableName+"'";
		try {
			String e =sqlcon.executeGetValue(sql);
			//System.out.println("---- "+e);
			if(e!=null&&e.equals(userTableName))
			{
				exist=true;
			}
		} catch (Exception e) {
			throw new DBException(DBException.DB_CONNECTION_ACCESS_DENIED,"Error check if table exist.");
		}

		return exist;
	}
	/**
	 * Returns the password of the specified user from the db.
	 * @return The password of the user in DB.
	 */
	private String getUserDBPassword()
	{
		return "";
	}
}   