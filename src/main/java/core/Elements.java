package core;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import utils.Log;


/**
 *
 * <p>A wrapper for selenium's {@code RemoteWebElement}. This class implements most {@code WebElement} operations,
 * and provides additional methods to simplify manipulation of waits, and various element functionality.</p>
 *
 * <p>Each {@code Elements} instance has a {@code locatorList}. The list allows fetching of the {@code WebElement}
 * to occur at the time of usage rather than instantiation. As such, an {@code Elements} object does not represent
 * a physical HTML / Android element, but an actor on an element. In general, this enables waits to account for the
 * actual element fetch and the subsequent action.</p>
 *
 * <p>Additionally, an {@code Elements} object is functionally a {@code List} and incorporates many of its defining
 * characteristics, but behaves as a single object when needed. Internally, the result from the element fetch is
 * stored as a {@code List<WebElement>}. Actions performed on an {@code Elements} instance is executed on the first
 * {@code WebElement} in the list. Access to other {@code WebElement}s is done through the {@code get()} method,
 * where a new {@code Elements} object is created to preserve the element wrapping.</p>
 *
 * <p>Instances of this class are iterable, and nullable. <br>
 * But, elements fetched are never empty - an {@code ElementsException} is thrown for elements that cannot
 * be found.</p>
 *
 *
 * @author Marko Zhen
 *
 */

public class Elements implements Iterable<Elements> {


	private List<WebElement> elementList;
	private String elementPath;

	private WebDriver driver;

	private List<LocatorEntry> locatorList;
	private static boolean overrideTimeout = false;

	private static final long defaultTimeOutInSeconds = 20;
	private static final long nanoSecond = 1000000000;


	/**
	 * The primary constructor used to instantiate a new {@code Elements} object.
	 *
	 * @param driver	An instance of an AndroidDriver or WebDriver
	 * @param locator	A By which identifies an element on the screen
     */
	public Elements(WebDriver driver, By locator) {
		this.driver = driver;
		this.elementList = null;			// Must be null
		this.elementPath = null;			// Must be null

		this.locatorList = new ArrayList<LocatorEntry>() {{ add(new ByLocatorEntry(locator)); }};
	}

	/**
	 * The internal constructor used to return a new {@code Elements} object for cascading lookups.
	 *
	 * @param driver		An instance of an AndroidDriver or WebDriver
	 * @param locatorList	A list of locators which identifies the path of an element on the screen
	 * @see					#get(int)
	 * @see					#findElements(By)
	 * @see					#constructNextElement(LocatorEntry)
     */
	private Elements(WebDriver driver, List<LocatorEntry> locatorList) {
		this.driver = driver;
		this.elementList = null;			// Must be null
		this.elementPath = null;			// Must be null
		this.locatorList = locatorList;
	}

	/**
	 * The internal constructor used to return a new {@code Elements} object for wrapping a resolved list.
	 * {@code Elements} created uses cached instances of resolved {@code WebElement}s - no further locator
	 * resolution is performed.
	 *
	 * @param driver		An instance of an AndroidDriver or WebDriver
	 * @param path			String path to the element
	 * @param elements		The cached {@code WebElement}
	 * @param locatorList	A list of locators which identifies the path of an element on the screen
	 * @see					#toList()
	 * @see					#iterator()
	 * @see					#constructCachedElement(WebElement, String, LocatorEntry)
     */
	private Elements(WebDriver driver, String path, List<WebElement> elements, List<LocatorEntry> locatorList) {
		this.driver = driver;
		this.elementPath = path;
		this.elementList = elements;
		this.locatorList = locatorList;
	}


	/**
	 * Executes the {@code Callable} method repeatedly until a non-null result is returned or the default timeout
	 * is reached. All {@code Exception}s are ignored during the loop.
	 *
	 * <p>See {@link #Wait(long, Callable)} for a detailed example.</p>
	 *
	 * @param method	The executing code block
	 * @param <T>		Return type of the code block
     * @return			The result of the executing block; or null if the default timeout was reached
	 * @see 			#Wait(long, Callable)
     */
	public static <T> T Wait(Callable<T> method) {
		return Wait(defaultTimeOutInSeconds, method);
	}

	/**
	 * Executes the {@code Callable} method repeatedly until a non-null result is returned or the specified timeout
	 * is reached. All {@code Exception}s are ignored during the loop.
	 *
	 * <p>Use this method to resolve guarding issues when performing nested {@code Elements} operations that require
	 * fetching of elements. In the below example, {@code getChecksList()} fetches an initial list of elements
	 * which is subsequently used by {@code getCheckNumber()}. Notice that if the initial fetch for the list of
	 * checks happens before the UI updates, the loop could fail to return the desired result. Thus, this method
	 * allows the {@code Callable} block to loop and re-fetch all necessary elements.</p>
	 *
	 * <p>Note: Using other wait methods inside the {@code Callable} block is not recommended and would lead to
	 * undefined behaviour</p>
	 *
	 * <pre>{@code Elements result = Elements.Wait(() -> {
	 *     for (Elements e : getChecksList()) {
	 *         if (getCheckNumber(e).getText().equals("#" + checkNumber)) {
	 *             return e;
	 *         }
	 *     }
	 *     return null;
	 * });
	 * }</pre>
	 *
	 * @param timeOutInSeconds		Time limit to execute the method
	 * @param method				The executing code block
	 * @param <T>					Return type of the code block
     * @return						The result of the executing block; or null if the timeout was reached
	 * @see							#Wait(Callable)
     */
	public static <T> T Wait(long timeOutInSeconds, Callable<T> method) {

		final long timeOut = System.nanoTime() + timeOutInSeconds * nanoSecond;
		Exception lastException = null;
		T result = null;

		// Guards against nested uses of this method
		// Does not prevent other waits being used inside the Callable block
		if (overrideTimeout) {
			throw new RuntimeException("ERROR : Undefined state in Elements.Wait()");
		}

		// Sets a flag which ignores fetch timeouts, forcing each explicit fetch to execute only once.
		// This ensures that control of waits, and retries occur as a whole. A fetch failure inside
		// the Callable block would therefore execute from the start of the Callable again rather than
		// retrying just the failed fetch.
		overrideTimeout = true;

		while (System.nanoTime() < timeOut && result == null) {
			try {

				Thread.sleep(100);
				result = method.call();

			} catch (Exception e) {
				// Keep track of latest exception & Continue execution until timeout
				lastException = e;
			}
		}

		overrideTimeout = false;

		if (result == null && lastException != null) {
			Log.warn("Ignored :  " + lastException.getClass() + " " + lastException.getMessage());
		}

		return result;
	}


	/**
	 * Fetches the element defined by the {@code locatorList} using the default timeout.
	 *
	 * @see		#resolveLocators(long)
	 */
	private void resolveLocators() {
		resolveLocators(System.nanoTime() + defaultTimeOutInSeconds * nanoSecond);
	}

	/**
	 * Fetches the element defined by the {@code locatorList} using the specified timeout.
	 *
	 * @param timeOutInNanoTime		{@code System.nanoTime()} which defines the time limit to resolve all locators
	 * @see							#resolveLocators()
     */
	private void resolveLocators(long timeOutInNanoTime) {

		if (locatorList == null || locatorList.isEmpty()) {
			throw new ElementsException("ERROR : resolveLocators() encountered a fatal error !");
		}

		// Resolution should only happen once for each Elements instance
		if (elementList != null && elementPath != null) {
			return;
		}

		// The result is only committed after complete resolution
		String tmpPath;
		List<WebElement> tmpResult;
		Exception lastException;


		// The nested do-while for loops ensures the fetch happens at least once regardless of the
		// specified timeout. Additionally, each resolution attempt resets and restarts the fetch from
		// the start of the locatorList to prevent stale data

		do {

			tmpResult = null;
			tmpPath = null;

			try {
				for (LocatorEntry entry : locatorList) {

					// timeOutInNanoTime is forwarded to ensure timeout conditions remain consistent
					// Fetch using GetLocatorEntry does not loop, thus do not need timeOutInNanoTime

					if (entry instanceof ByLocatorEntry) {

						ByLocatorEntry locator = (ByLocatorEntry) entry;
						tmpPath = ( (tmpPath == null) ? "" : tmpPath + " + " ) + locator.getValue().toString();
						tmpResult = fetchElements(locator, tmpResult, tmpPath, timeOutInNanoTime);

					} else if (entry instanceof GetLocatorEntry) {

						GetLocatorEntry locator = (GetLocatorEntry) entry;
						tmpPath = tmpPath + "[" + locator.getValue() + "]";
						tmpResult = fetchElements(locator, tmpResult, tmpPath);

					} else {
						throw new ElementsException("ERROR : undefined LocatorEntry in resolveLocators()");
					}
				}

				elementList = tmpResult;
				elementPath = tmpPath;
				return;

			} catch (Exception e) {
				// Keep track of latest exception & Continue searching until timeout
				lastException = e;
			}

		} while (!overrideTimeout && System.nanoTime() < timeOutInNanoTime);

		// Element cannot be found
		throw new ElementsException("ERROR : Failed to resolveLocators() within allotted time", lastException);
	}


	/**
	 * Fetches the element defined by the {@code GetLocatorEntry}. The result is always non-empty and will throw
	 * an exception if the element cannot be found.
	 *
	 * @param locator			An instance of {@code GetLocatorEntry}
	 * @param elementList		A non-empty list of elements
	 * @param elementPath		String representation of the element's path for logs
     * @return					A non-empty list containing the result as a single entry
	 * @see						#resolveLocators()
	 * @see						#resolveLocators(long)
	 */
	private List<WebElement> fetchElements(GetLocatorEntry locator, List<WebElement> elementList, String elementPath) {

		Log.info("Looking for " + elementPath + " ...");

		WebElement result = elementList.get(locator.getValue());

		if (result == null) {
			throw new ElementsException("ERROR : fetchElements() returned null");
		}

		return new ArrayList<WebElement>() {{ add(result); }};
	}


	/**
	 * Fetches the element defined by the {@code ByLocatorEntry}. The result is always non-empty and will throw
	 * an exception if the element cannot be found.
	 *
	 * @param locator				An instance of {@code ByLocatorEntry}
	 * @param elementList			A list of elements, can be null
	 * @param elementPath			String representation of the element's path for logs
	 * @param timeOutInNanoTime		{@code System.nanoTime()} which defines the time limit to fetch the element
	 * @return						A non-empty list containing the result
     * @throws Exception			If element not found; The last exception caught or an {@code ElementsException}
	 * @see							#resolveLocators()
	 * @see							#resolveLocators(long)
     */
	private List<WebElement> fetchElements(ByLocatorEntry locator, List<WebElement> elementList, String elementPath,
			long timeOutInNanoTime) throws Exception {

		Log.info("Looking for " + elementPath + " ...");

		List<WebElement> result = null;
		Exception lastException = null;

		do {
			try {

				Thread.sleep(100);

				if (elementList != null && !elementList.isEmpty()) {
					result = elementList.get(0).findElements(locator.getValue());

				} else if (driver != null) {
					result = driver.findElements(locator.getValue());
				}

				// Both elementList & driver is null, thus result will be null

			} catch (Exception e) {
				// Keep track of latest exception & continue searching until timeout
				lastException = e;
			}

		} while (!overrideTimeout && (result == null || result.isEmpty()) && System.nanoTime() < timeOutInNanoTime);


		if (result == null) {
			throw new ElementsException("ERROR : fetchElements() returned null");

		} else if (result.isEmpty()) {

			if (lastException != null) { throw lastException; }
			throw new ElementsException("ERROR : Element not found within allotted time");
		}

		return result;
	}


	/**
	 * Applies a timeout in seconds to the current {@code System.nanoTime()}
	 *
	 * @param timeOutInSeconds		The desired timeout in seconds
	 * @return						{@code System.nanoTime()} + timeOutInSeconds
     */
	private long getTimeOutInNanoTime(long timeOutInSeconds) {
		return System.nanoTime() + timeOutInSeconds * nanoSecond;
	}

	/**
	 * Calculates the remaining time in seconds between the current {@code System.nanoTime()} and the
	 * specified nanoTime.
	 *
	 * @param timeOutInNanoTime		The provided {@code System.nanoTime()} value
	 * @return						The difference between the current and provided nanoTime in seconds; min. 1 sec
     */
	private long getRemainingTimeInSeconds(long timeOutInNanoTime) {

		// A minimum of 1 second ensures that waits are not performed on 0 secs
		return Math.max(1, Math.floorDiv( Math.max(0, (timeOutInNanoTime - System.nanoTime())) , nanoSecond));
	}


	/**
	 * Creates a new {@code Elements} instance for child / cascading elements
	 *
	 * @param locator	{@code LocatorEntry} of the child element
	 * @return			A new {@code Elements} instance
	 * @see				#get(int)
	 * @see				#findElements(By)
	 * @see				#Elements(WebDriver, List)
     */
	private Elements constructNextElement(LocatorEntry locator) {
		List<LocatorEntry> tmpLocatorList = new ArrayList<>(locatorList);
		tmpLocatorList.add(locator);

		return new Elements(driver, tmpLocatorList);
	}

	/**
	 * Creates a new {@code Elements} instance for wrapping a resolved {@code WebElement}
	 *
	 * @param element	The resolved {@code WebElement}
	 * @param path		String representation of the element path
	 * @param locator	{@code LocatorEntry} of the child element
     * @return			A new {@code Elements} instance with an existing fetched element
	 * @see				#toList()
	 * @see				#iterator()
	 * @see				#Elements(WebDriver, String, List, List)
     */
	private Elements constructCachedElement(WebElement element, String path, LocatorEntry locator) {
		List<LocatorEntry> tmpLocatorList = new ArrayList<>(locatorList);
		tmpLocatorList.add(locator);

		return new Elements(driver, path, new ArrayList<WebElement>() {{ add(element); }}, tmpLocatorList);
	}


	/**
	 * Converts an {@code Elements} instance into an explicit list of wrapped elements
	 *
	 * @return		A list of {@code Elements}
     */
	public List<Elements> toList() {

		resolveLocators();

		ArrayList<Elements> wrappedList = new ArrayList<>();

		for (int i = 0; i < elementList.size(); i++) {

			// Each element in the elementList is already resolved from the initial fetch
			// constructCachedElement() is used to prevent re-fetching and increase performance in element loops
			// Resolution in cascading lookups is maintained through constructNextElement()
			wrappedList.add( constructCachedElement( elementList.get(i), elementPath + "[" + i + "]", new GetLocatorEntry(i) ));
		}

		return wrappedList;
	}

	@Override
	public Iterator<Elements> iterator() {
		return toList().iterator();
	}


	/**
	 * Returns a wrapped element at the specified position in this list.
	 *
	 * @param index		Index of the element to return
	 * @return			The element at the specified position in this list
     */
	public Elements get(int index) {
		return constructNextElement(new GetLocatorEntry(index));
	}

	/**
	 * Finds all elements satisfying the provided locator that are a children of this element.
	 *
	 * <p>Note: An XPath locator cannot be used for Android elements due to Appium limitations.</p>
	 *
	 * @param locator	A By which identifies an element on the screen
	 * @return			A new {@code Elements} instance
     */
	public Elements findElements(By locator) {
		return constructNextElement(new ByLocatorEntry(locator));
	}


	/**
	 * Returns the bare {@code WebElement}
	 *
	 * @return		The standalone {@code WebElement} without the features of this class
     */
	public WebElement getRawElement() {
		resolveLocators();
		Log.info("Fetching WebElement from " + elementPath);

		return elementList.get(0);
	}

	/**
	 * Returns the number of elements in this list; 0 if the element cannot be found.
	 *
	 * @return		The number of elements in this list
     */
	public int size() {
		try {
			resolveLocators();
			Log.info("Size of " + elementPath + " is : " + elementList.size());
			return elementList.size();

		} catch (Exception e) {
			Log.warn("Element cannot be found;  Size is 0");
			Log.warn("Ignored :  " + e.getCause().getClass() + " " + e.getCause().getMessage());
			return 0;
		}
	}

	/**
	 * Returns true if the element cannot be found (empty)
	 *
	 * @return		True if the element cannot be found, false otherwise
     */
	public boolean isEmpty() {
		return size() < 1;
	}

	/**
	 * Determine whether or not this element is selected or not.
	 *
	 * @return		True if the element is currently selected, false otherwise
     */
	public boolean isSelected() {
		resolveLocators();
		Log.info("Executing isSelected() on " + elementPath);

		return elementList.get(0).isSelected();
	}

	/**
	 * Determine whether or not this element is checked or not by detecting the "checked" attribute.
	 *
	 * @return		True if the element is currently checked, false otherwise
     */
	public boolean isChecked() {
		resolveLocators();
		Log.info("Executing isChecked() on " + elementPath);

		return elementList.get(0).getAttribute("class").contains("checked") || elementList.get(0).getAttribute("checked") != null;
	}

	/**
	 * Determine whether or not this element is enabled or not.
	 *
	 * @return		True if the element is currently enabled, false otherwise
	 * @see			#waitForEnabled()
	 * @see			#waitForEnabled(long)
     */
	public boolean isEnabled() {
		resolveLocators();
		Log.info("Executing isEnabled() on " + elementPath);

		return elementList.get(0).isEnabled();
	}

	/**
	 * Determine whether or not this element is visible or not.
	 *
	 * @return		True if the element is currently visible, false otherwise
	 * @see			#waitForVisible()
	 * @see			#waitForVisible(long)
	 * @see			#waitForNotVisible()
	 * @see			#waitForNotVisible(long)
     */
	public boolean isDisplayed() {
		resolveLocators();
		Log.info("Executing isDisplayed() on " + elementPath);

		return elementList.get(0).isDisplayed();
	}

	/**
	 * Get the visible (i.e. not hidden by CSS) innerText of this element, including sub-elements, without any
	 * leading or trailing whitespace.
	 *
	 * @return		The innerText of this element
	 * @see			#waitForTextVisible(String)
	 * @see			#waitForTextVisible(String, long)
     */
	public String getText() {
		resolveLocators();
		Log.info("Executing getText() on " + elementPath);

		return elementList.get(0).getText();
	}

	/**
	 * Gets the name of the element's tag such as input, button, TextView, etc.
	 *
	 * @return		The tag name of this element
     */
	public String getTagName() {
		resolveLocators();
		Log.info("Executing getTagName() on " + elementPath);

		return elementList.get(0).getTagName();
	}

	/**
	 * Gets the value of the given CSS property
	 *
	 * @param value		The name of the CSS property
	 * @return			The current value of the CSS property
     */
	public String getCssValue(String value) {
		resolveLocators();
		Log.info("Executing getCssValue(" + value + ") on " + elementPath);

		return elementList.get(0).getCssValue(value);
	}

	/**
	 * Gets the value of the provided attribute.
	 *
	 * @param attribute		The name of the attribute
	 * @return				The current value at the specified attribute, or null if the value is not set
     */
	public String getAttribute(String attribute) {
		resolveLocators();
		Log.info("Executing getAttribute(" + attribute + ") on " + elementPath);

		return elementList.get(0).getAttribute(attribute);
	}

	/**
	 * Gets the X,Y coordinates of the top-left corner of where the element is rendered on the screen.
	 *
	 * @return		A point, containing the location of the top-left corner of the element
     */
	public Point getLocation() {
		resolveLocators();
		Log.info("Executing getLocation() on " + elementPath);

		return elementList.get(0).getLocation();
	}

	/**
	 * Gets the physical rendered size of the element on the screen.
	 *
	 * @return		The width and length of the element
     */
	public Dimension getElementSize() {
		resolveLocators();
		Log.info("Executing getElementSize() on " + elementPath);

		return elementList.get(0).getSize();
	}


	/**
	 * Clears the value of text input elements. This method ensures the element is visible and enabled before
	 * executing the action. The default timeout is used.
	 *
	 * @see		#sendKeys(CharSequence...)
	 */
	public void clear() {
		resolveLocators();

		if (waitForEnabled()) {
			Log.info("Executing clear() on " + elementPath);
			elementList.get(0).clear();

		} else {
			throw new ElementsException("ERROR : Failed to clear() " + elementPath);
		}
	}


	/**
	 * Simulates typing into a text input element. This method ensures the element is visible and enabled before
	 * executing the action. The default timeout is used.
	 * Additionally, on Android elements, it will attempt to dismiss the keyboard afterwards.
	 *
	 * @param keys		The text to send to the element
	 * @see				#clear()
     */
	public void sendKeys(CharSequence ... keys) {
		resolveLocators();

		if (waitForEnabled()) {
			Log.info("Sending keys to " + elementPath);
			elementList.get(0).sendKeys(keys);

			if (driver instanceof AndroidDriver) {
				try {

					Log.info("sendKeys() successfully completed, dismissing keyboard");
					((AndroidDriver) driver).hideKeyboard();

				} catch (Exception e) {
					Log.warn("Ignored :  " + e.getClass() + " " + e.getMessage());
				}
			}

		} else {
			throw new ElementsException("ERROR : Failed to sendKeys() to " + elementPath);
		}
	}


	/**
	 * Click / tap on an element. This method ensures the element is visible and enabled before executing the
	 * action. The default timeout is used.
	 */
	public void click() {
		resolveLocators();

		if (waitForEnabled()) {

			if (driver instanceof AndroidDriver) {
				Log.info("Tapping " + elementPath);
				new TouchAction((AndroidDriver) driver).tap(elementList.get(0)).perform();

			} else {
				Log.info("Clicking " + elementPath);
				elementList.get(0).click();
			}

		} else {
			throw new ElementsException("ERROR : Failed to click() " + elementPath);
		}
	}


	/**
	 * Scrolls to and performs a click on an element using a {@code JavascriptExecutor}. This method can only be
	 * used by a {@code WebDriver} for a web-based element. The default timeout is used.
	 */
	public void javascriptClick() {

		resolveLocators();
		Log.info("Executing javascriptClick() on " + elementPath);

		if (driver instanceof AndroidDriver) {
			throw new ElementsException("ERROR : javascriptClick() cannot be performed on Android Element");
		}

		long timeout = System.nanoTime() + defaultTimeOutInSeconds * nanoSecond;

		while (true) {
			try {

				Thread.sleep(100);

				if (System.nanoTime() > timeout) {
					throw new TimeoutException();
				}

				JavascriptExecutor executor = (JavascriptExecutor) Driver.web;
				executor.executeScript("arguments[0].scrollIntoView();", elementList.get(0));
				executor.executeScript("arguments[0].click();", elementList.get(0));

				break;

			} catch (TimeoutException e) {
				throw new ElementsException("ERROR : Failed to javascriptClick() " + elementPath + " within allotted time");

			} catch (Exception e) {
				// Try to perform javascriptClick() again until default timeout
			}
		}
	}


	/**
	 * Waits for an element to become enabled using the default timeout. Resolution of locators is included in
	 * the time limit.
	 *
	 * @return		True if the element is enabled, false otherwise
	 * @see			#waitForEnabled(long)
	 * @see			#isEnabled()
     */
	public boolean waitForEnabled() {
		return waitForEnabled(defaultTimeOutInSeconds);
	}

	/**
	 * Waits for an element to become enabled using the specified timeout. Resolution of locators is included in
	 * the time limit.
	 *
	 * @param timeOutInSeconds		The desired time limit of the wait
	 * @return						True if the element is enabled, false otherwise
	 * @see							#waitForEnabled()
	 * @see							#isEnabled()
     */
	public boolean waitForEnabled(long timeOutInSeconds) {

		long timeOut = getTimeOutInNanoTime(timeOutInSeconds);
		resolveLocators(timeOut);

		Log.info("Waiting for " + elementPath + " to become enabled ...");

		try {
			WebDriverWait wait = (WebDriverWait) new WebDriverWait(driver, getRemainingTimeInSeconds(timeOut)).ignoring(Exception.class);
			wait.until(ExpectedConditions.elementToBeClickable(elementList.get(0)));
			return true;

		} catch (Exception e) {
			Log.warn("WARNING : Element not enabled within allotted time");
			return false;
		}

	}


	/**
	 * Waits for an element to become selected using the default timeout. Resolution of locators is included in
	 * the time limit.
	 *
	 * @return		True if the element is selected, false otherwise
	 * @see			#waitForSelected(long)
	 * @see			#isSelected()
     */
	public boolean waitForSelected() {
		return waitForSelected(defaultTimeOutInSeconds);
	}

	/**
	 * Waits for an element to become selected using the specified timeout. Resolution of locators is included in
	 * the time limit.
	 *
	 * @param timeOutInSeconds		The desired time limit of the wait
	 * @return						True if the element is selected, false otherwise
	 * @see							#waitForSelected()
	 * @see							#isSelected()
     */
	public boolean waitForSelected(long timeOutInSeconds) {

		long timeOut = getTimeOutInNanoTime(timeOutInSeconds);
		resolveLocators(timeOut);

		Log.info("Waiting for " + elementPath + " to become selected ...");

		try {
			WebDriverWait wait = (WebDriverWait) new WebDriverWait(driver, getRemainingTimeInSeconds(timeOut)).ignoring(Exception.class);
			wait.until(ExpectedConditions.elementToBeSelected(elementList.get(0)));
			return true;

		} catch (Exception e) {
			Log.warn("WARNING : Element not selected within allotted time");
			return false;
		}

	}


	/**
	 * Waits for an element to become visible using the default timeout. Resolution of locators is included in
	 * the time limit.
	 *
	 * @return		True if the element is visible, false otherwise
	 * @see			#waitForVisible(long)
	 * @see			#waitForNotVisible()
	 * @see			#waitForNotVisible(long)
	 * @see			#isDisplayed()
     */
	public boolean waitForVisible() {
		return waitForVisible(defaultTimeOutInSeconds);
	}

	/**
	 * Waits for an element to become visible using the specified timeout. Resolution of locators is included in
	 * the time limit.
	 *
	 * @param timeOutInSeconds		The desired time limit of the wait
	 * @return						True if the element is visible, false otherwise
	 * @see							#waitForVisible()
	 * @see							#waitForNotVisible()
	 * @see							#waitForNotVisible(long)
	 * @see							#isDisplayed()
     */
	public boolean waitForVisible(long timeOutInSeconds) {

		long timeOut = getTimeOutInNanoTime(timeOutInSeconds);

		// If the element cannot be found, return false
		try {
			resolveLocators(timeOut);
		} catch (Exception e) {
			Log.info("Waiting for element to become visible ...");
			Log.warn("Failed to resolveLocators() within allotted time - Element not visible");
			Log.warn("Ignored :  " + e.getCause().getClass() + " " + e.getCause().getMessage());
			return false;
		}

		Log.info("Waiting for " + elementPath + " to become visible ...");

		try {
			WebDriverWait wait = (WebDriverWait) new WebDriverWait(driver, getRemainingTimeInSeconds(timeOut)).ignoring(Exception.class);
			wait.until(ExpectedConditions.visibilityOf(elementList.get(0)));
			return true;

		} catch (Exception e) {
			Log.warn("WARNING : Element not visible within allotted time");
			return false;
		}
	}


	/**
	 * Waits for an element to disappear using the default timeout. Resolution of locators is included in
	 * the time limit.
	 *
	 * @return		True if the element is not visible, false otherwise
	 * @see			#waitForNotVisible(long)
	 * @see			#waitForVisible()
	 * @see			#waitForVisible(long)
	 * @see			#isDisplayed()
     */
	public boolean waitForNotVisible() {
		return waitForNotVisible(defaultTimeOutInSeconds);
	}

	/**
	 * Waits for an element to disappear using the specified timeout. Resolution of locators is included in
	 * the time limit.
	 *
	 * @param timeOutInSeconds		The desired time limit of the wait
	 * @return						True if the element is not visible, false otherwise
	 * @see							#waitForNotVisible()
	 * @see							#waitForVisible()
	 * @see							#waitForVisible(long)
	 * @see							#isDisplayed()
	 */
	public boolean waitForNotVisible(long timeOutInSeconds) {

		long timeOut = getTimeOutInNanoTime(timeOutInSeconds);

		// If the element cannot be found, return true
		try {
			resolveLocators(timeOut);
		} catch (Exception e) {
			Log.info("Waiting for element to disappear ...");
			Log.warn("Failed to resolveLocators() within allotted time - Element successfully disappeared");
			Log.warn("Ignored :  " + e.getCause().getClass() + " " + e.getCause().getMessage());
			return true;
		}

		Log.info("Waiting for " + elementPath + " to disappear ...");

		try {
			WebDriverWait wait = new WebDriverWait(driver, getRemainingTimeInSeconds(timeOut));
			wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOf(elementList.get(0))));
			return true;

		} catch (NoSuchElementException | StaleElementReferenceException e) {
			// Element was physically removed from the page
			return true;

		} catch (Exception e) {
			Log.warn("WARNING : Element did not disappear within allotted time");
			return false;
		}
	}


	/**
	 * Waits for the given text to appear in the element using the default timeout. Resolution of locators is
	 * included in the time limit.
	 *
	 * @param text		The condition text
	 * @return			True if the element contains the text, false otherwise
	 * @see				#getText()
     */
	public boolean waitForTextVisible(String text) {
		return waitForTextVisible(text, defaultTimeOutInSeconds);
	}

	/**
	 * Waits for the given text to appear in the element using the specified timeout. Resolution of locators
	 * is included in the time limit.
	 *
	 * @param text					The condition text
	 * @param timeOutInSeconds		The desired time limit of the wait
     * @return						True if the element contains the text, false otherwise
	 * @see							#getText()
     */
	public boolean waitForTextVisible(String text, long timeOutInSeconds) {

		long timeOut = getTimeOutInNanoTime(timeOutInSeconds);
		resolveLocators(timeOut);

		Log.info("Waiting for " + elementPath + " to contain text '" + text + "' ...");

		try {
			WebDriverWait wait = (WebDriverWait) new WebDriverWait(driver, getRemainingTimeInSeconds(timeOut)).ignoring(Exception.class);
			wait.until(ExpectedConditions.textToBePresentInElement(elementList.get(0), text));
			return true;

		} catch (Exception e) {
			Log.warn("WARNING : Text not found within allotted time");
			return false;
		}
	}



	private interface LocatorEntry<T> {
		T getValue();
	}

	private class GetLocatorEntry implements LocatorEntry<Integer> {

		private int value = -1;

		public GetLocatorEntry(int indexValue) {
			this.value = indexValue;
		}

		@Override
		public Integer getValue() {
			return value;
		}
	}

	private class ByLocatorEntry implements LocatorEntry<By> {

		private By value = null;

		public ByLocatorEntry(By locatorValue) {
			this.value = locatorValue;
		}

		@Override
		public By getValue() {
			return value;
		}
	}

	private class ElementsException extends RuntimeException {

		public ElementsException(String message) {
			super(message);
		}

		public ElementsException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
