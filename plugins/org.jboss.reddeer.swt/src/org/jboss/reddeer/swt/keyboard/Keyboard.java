/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.reddeer.swt.keyboard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.jboss.reddeer.common.logging.Logger;
import org.jboss.reddeer.core.lookup.WidgetLookup;
import org.jboss.reddeer.core.util.Display;
import org.jboss.reddeer.swt.exception.SWTLayerException;

/**
 * Class for operating with keyboard
 * 
 * @author rhopp
 * 
 */

abstract public class Keyboard {
	
	private static final Logger log = Logger.getLogger(Keyboard.class);
	
	private static final int DELAY = 200;
	
	/**
	 * Invokes given key combination. Accepts chars or {@link org.eclipse.swt.SWT} constants. For example: invokeKeyCombination(SWT.CONTROL, SWT.SHIFT, 't');
	 * 
	 * @param keys either chars or values from SWT.KeyCombination
	 * @see org.eclipse.swt.SWT
	 */
	
	public void invokeKeyCombination(int... keys){
		final Widget w = WidgetLookup.getInstance().getFocusControl();
		log.info("Invoke key combination: ");
		for (int i=0; i<keys.length; i++){
			log.info("    As char:" + (char) keys[i] + ", as int:" + keys[i]);
			sync();
			Display.getDisplay().post(keyEvent(keys[i], SWT.KeyDown, w));
		}
		for (int i=keys.length-1; i>=0; i--){
			sync();
			Display.getDisplay().post(keyEvent(keys[i], SWT.KeyUp, w));
			sync();
		}
	}
	
	private void invokeKeyCombinationForOneCharacter(char c, int... keys){
		if (keys.length != 2){
			throw new SWTLayerException("Chybka: keys.length !=2");
		}
		int modifiers = keys[0];
		if (modifiers == (SWT.CTRL | SWT.ALT)){
			final Widget w = WidgetLookup.getInstance().getFocusControl();
			log.info("Invoke key combination: ");
			for (int i=0; i<keys.length; i++){
				log.info("    As char:" + (char) keys[i] + ", as int:" + keys[i]);
				sync();
				Display.getDisplay().post(keyEvent(keys[i], SWT.KeyDown, w, c));
			}
			for (int i=keys.length-1; i>=0; i--){
				sync();
				Display.getDisplay().post(keyEvent(keys[i], SWT.KeyUp, w, c));
				sync();
			}
		}else{
			invokeKeyCombination(keys);
		}
	}
	
	/**
	 * Types given text.
	 *
	 * @param text the text to type
	 */
	
	public void type(String text){
		log.info("Type text \"" + text + "\"");
		for (char c : text.toCharArray()) {
			invokeKeyCombinationForOneCharacter(c, DefaultKeyboardLayout.getInstance().getKeyCombination(c));
		}
	}
	
	/**
	 *  Types given character.
	 *
	 * @param c the character to type
	 */
	
	public void type(int c){
		log.info("Type character '" + (char) c + "', as int:" + c);
		press(c);
		release(c);
	}
	
	/**
	 * Selects `shift` characters to the side of cursor specified by `toLeft`.
	 *
	 * @param shift number of characters to select
	 * @param toLeft true for left, false for right
	 */
	
	public void select(int shift, boolean toLeft){
		if (toLeft){
			log.info("Select "+shift+" characters to the left");
		}else{
			log.debug("Select "+shift+" characters to the right");
		}
		press(SWT.SHIFT);
		moveCursor(shift, toLeft);
		release(SWT.SHIFT);
	}
	
	/**
	 * Moves cursor by `shift` characters to the side of cursor specified by `toLeft`.
	 *
	 * @param shift number of characters to move
	 * @param toLeft true for left, false for right
	 */
	
	public void moveCursor(int shift, boolean toLeft){
		log.info("Move cursor");
		for (int i=0; i<shift; i++){
			sync();
			if (toLeft){
				type(SWT.ARROW_LEFT);
			}else{
				type(SWT.ARROW_RIGHT);
			}
		}
	}
	
	/**
	 * Either cuts or copies selected text to clipboard.
	 *
	 * @param cut cuts the text if true, copies otherwise
	 */
	
	abstract public void writeToClipboard(boolean cut);
	
	/**
	 * Pastes text stored in clipboard.
	 */
	
	abstract public void pasteFromClipboard();
	
	
	protected void press(final int key){
		log.debug("Press character '" + (char) key + "', as int:" + key);
		final Widget w = WidgetLookup.getInstance().getFocusControl();
		sync();
		Display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				Display.getDisplay().post(keyEvent(key, SWT.KeyDown, w));
			}
		});
	}
	
	protected void release(final int key){
		log.debug("Release character '" + (char) key + "', as int:" + key);
		final Widget w = WidgetLookup.getInstance().getFocusControl();
		Display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				Display.getDisplay().post(keyEvent(key, SWT.KeyUp, w));
			}
		});
		sync();
	}
	
	private Event keyEvent(int key, int eventType, Widget w){
		return keyEvent(key, eventType, w, (char) key);
	}
	
	private Event keyEvent(int key, int eventType, Widget w, char character){
		System.out.println("Creating key event with keyCode: "+key+" and character: "+character);
		Event e = new Event();
		e.keyCode = key;
		e.character = (char) character;
		e.type = eventType;
		e.widget = w;
		if (character == '>'){
			System.out.println("ALT!");
			e.stateMask=SWT.ALT;
		}
		return e;
	}
	
	private void sync() {
		delay(DELAY);
		emptySync();		
	}

	private void emptySync() {
		Display.syncExec(new Runnable() {
			
			@Override
			public void run() {				
			}
		});
		
	}
	
	private void delay(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
