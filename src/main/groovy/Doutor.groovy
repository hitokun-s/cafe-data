import groovyx.net.http.RESTClient
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver

class Doutor {

    static void main(String[] args){
        
        def fileName = "doutor.csv"

//        def driver = new HtmlUnitDriver(BrowserVersion.INTERNET_EXPLORER_11)
        def driver = new ChromeDriver()
        
        def geoCodingUrl = "https://maps.googleapis.com/maps/api/geocode/"
        def client = new RESTClient( geoCodingUrl )

        new File(fileName).text = ""
        
        def exec = { prefectureIdx ->
            
            if((prefectureIdx + "").size() == 1){
                prefectureIdx = "0" + prefectureIdx
            }
            driver.get("http://sasp.mapion.co.jp/b/doutor/attr/?kencode=$prefectureIdx&t=attr_con")
            println driver.title

            def lines = "" 
            
            boolean toContinue = true
            
            while(toContinue){
                def storeElements = driver.findElementByClassName("MapiTable").findElements(By.tagName("tr"))
                println "${storeElements.size()} stores found."

                storeElements.collect{ WebElement elm ->
                    def storeName = elm.findElement(By.tagName("dt")).findElement(By.tagName("a")).text;
                    def storeAddress = elm.findElement(By.className("MapiInfoAddr")).text;
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

                try{
                    def goNextElm = driver.findElementById("m_nextpage_link")
                    if(goNextElm){
                        goNextElm.click()
                    }else{
                        toContinue = false
                    }
                }catch(Exception e){
                    toContinue = false
                }
                
            }
        }

        (1..47).each(exec)
        
        driver.close();
            
    }
}
