package me.whizvox.otdl.user;

public enum UserRole {

  /**
   * A logged-in user whose account has been flagged as "restricted", and is unable to perform any actions that can
   * add or modify any resources.
   */
  RESTRICTED,

  /**
   * A user who is not logged in.
   */
  GUEST,

  /**
   * The default role given to a newly-created account.
   */
  MEMBER,

  /**
   * A member that has donated enough to warrant special permissions, such as larger upload limit, longer file
   * lifespans, and custom file expiration durations.
   */
  CONTRIBUTOR,

  /**
   * A logged-in user that has access to all possible endpoints of the site.
   */
  ADMIN

}
