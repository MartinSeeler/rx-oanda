package rx.oanda.account

sealed trait AccountFinancingMode

/**
 * No financing is paid/charged for open Trades in the Account.
 */
case object NoFinancing extends AccountFinancingMode

/**
 * Second-by-second financing is paid/charged for open Trades in the Account, both daily and when the the Trade is closed.
 */
case object SecondBySecond extends AccountFinancingMode

/**
 * A full day’s worth of financing is paid/charged for open Trades in the Account daily at 5pm New York time.
 */
case object Daily extends AccountFinancingMode
