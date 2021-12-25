package com.dalti.laposte.core.ui;

import junit.framework.TestCase;

public class NoteStateTest extends TestCase {

    public void testName() {
        int bigIconIndex = 0xFFF;
        int bigIconColorIndex = 0xF;

        int state = 0;
        state = NoteState.setFirstIcon(bigIconIndex, state);
        state = NoteState.setSecondIcon(bigIconIndex, state);
        state = NoteState.setFirstIconColor(bigIconColorIndex, state);
        state = NoteState.setSecondIconColor(bigIconColorIndex, state);
//        state = NoteState.from(bigIconIndex, bigIconColorIndex, bigIconIndex, bigIconColorIndex);

        System.out.println("(bigIconIndex == NoteState.getFirstIconIndex(state)) = " + (bigIconIndex == NoteState.getFirstIconIndex(state)));
        System.out.println("(bigIconIndex == NoteState.getFirstIconIndex(state)) = " + (bigIconIndex == NoteState.getSecondIconIndex(state)));
        System.out.println("(bigIconIndex == NoteState.getFirstIconIndex(state)) = " + (bigIconColorIndex == NoteState.getFirstIconColorIndex(state)));
        System.out.println("(bigIconIndex == NoteState.getFirstIconIndex(state)) = " + (bigIconColorIndex == NoteState.getSecondIconColorIndex(state)));
    }

    public void test2() {
        int state = 5246995;
        System.out.println("NoteState.getFirstIconIndex(state) = " + NoteState.getFirstIconIndex(state));
        System.out.println("NoteState.getSecondIconIndex(state) = " + NoteState.getSecondIconIndex(state));
        System.out.println("NoteState.getFirstIconColorIndex(state) = " + NoteState.getFirstIconColorIndex(state));
        System.out.println("NoteState.getSecondIconColorIndex(state) = " + NoteState.getSecondIconColorIndex(state));
    }
}