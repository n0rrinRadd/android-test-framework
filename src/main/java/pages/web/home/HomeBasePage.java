package pages.web.home;

import org.openqa.selenium.By;

import core.Driver;
import core.Elements;

public class HomeBasePage
{
    public Elements restaurantTitle(){
        By locator = By.xpath("//a[@class='restaurant-title']");
        return new Elements(Driver.web, locator);
    }

}