package com.zhan.budget.Adapter.Helper.Swipe;


/**
 * Determines what type of button was clicked on the bottom layout of the swipe.
 * Used to make sure when we close the swipe layout smoothly that when it finishes the animation, it
 * calls the callback to change the data via approve or unapprove.
 */
public enum SwipeClick{
    Normal,
    Approve,
    Unapprove,
    Delete
}