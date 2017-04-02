import groovyx.net.http.RESTClient
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver

class Starbucks {

    static void main(String[] args){

        def fileName = "starbucks.csv"

//        def driver = new HtmlUnitDriver(BrowserVersion.INTERNET_EXPLORER_11)
        def driver = new ChromeDriver()
        
        def geoCodingUrl = "https://maps.googleapis.com/maps/api/geocode/"
        def client = new RESTClient( geoCodingUrl )

        new File(fileName).text = ""
        
        def exec = { prefectureIdx ->
            driver.get("http://www.starbucks.co.jp/store/search/result.php?search_type=1&pref_code=$prefectureIdx")
            println driver.title
            
            def moreButton = driver.findElementById("moreList")
            def displayStatus = moreButton.getCssValue("display")
            
            while(displayStatus == "block"){
                moreButton.click()
                moreButton = driver.findElementById("moreList")
                displayStatus = moreButton.getCssValue("display")
            }
            
            def lines = "" 
            
            def storeElements = driver.findElementsByClassName("detailContainer")
            println "${storeElements.size()} stores found."

            storeElements.collect{ WebElement elm ->
                def storeName = elm.findElement(By.className("storeName")).text;
                def storeAddress = elm.findElement(By.className("storeAddress")).text;
                try{
                    def resp = client.get( path: "json", query:[address: storeAddress] )
                    def location = resp.data.results[0].geometry.location
                    def line = [storeName, storeAddress, location.lat, location.lng].collect{"\"$it\""}.join(",") + "\n"
                    lines += line
                }catch(Exception e){
                    println e
                    println "storeAddress: $storeAddress"
                }
                Thread.sleep(2000) // for Google API blocking
            }
            def file = new File(fileName)
            file.append(lines)
        }

        (1..47).each(exec)
        
        driver.close();
            
    }
}
