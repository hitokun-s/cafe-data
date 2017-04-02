import groovyx.net.http.RESTClient
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver

class Tullys {

    static void main(String[] args){
        
        def fileName = "tullys.csv"

//        def driver = new HtmlUnitDriver(BrowserVersion.INTERNET_EXPLORER_11)
        def driver = new ChromeDriver()
        
        def geoCodingUrl = "https://maps.googleapis.com/maps/api/geocode/"
        def client = new RESTClient( geoCodingUrl )

        new File(fileName).text = ""
        
        def exec = { prefecture ->
            
            println "Let's go to $prefecture"
            driver.get("https://map.tullys.co.jp/tullys/articleList?account=tullys&accmd=0&ftop=1&key=$prefecture&bitemtype=1&c21=1")

            def lines = "" 
            
            boolean toContinue = true
            
            while(toContinue){
                def storeElements = driver.findElementByClassName("list_table").findElements(By.tagName("tr")).findAll{
                    it.findElements(By.tagName("td"))
                }
                println "${storeElements.size()} stores found."

                storeElements.collect{ WebElement elm ->
                    def tds = elm.findElements(By.tagName("td"))
                    def storeName = tds[0].findElement(By.tagName("a")).text
                    def storeAddress = tds[1].text;
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

                def goNextElms = driver.findElementsByClassName("page_list__item")
                if(goNextElms){
                    try{
                        def nextLink = goNextElms[-1].findElement(By.tagName("a"))
                        if(nextLink && nextLink.text == "次へ"){
                            nextLink.click()
                            Thread.sleep(1000)
                        }
                    }catch(Exception e){
                        toContinue = false
                    }
                }else{
                    toContinue = false
                }
            }
        }
        
        def prefectures = new File("src/main/resources/prefectures.txt").text
        prefectures.split("\n").each(exec)
        
        driver.close();
            
    }
}
