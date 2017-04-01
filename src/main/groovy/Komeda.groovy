import groovyx.net.http.RESTClient
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver

class Komeda {

    static void main(String[] args){
        
        def fileName = "komeda.csv"

//        def driver = new HtmlUnitDriver(BrowserVersion.INTERNET_EXPLORER_11)
        def driver = new ChromeDriver()
        
        def geoCodingUrl = "https://maps.googleapis.com/maps/api/geocode/"
        def client = new RESTClient( geoCodingUrl )

        new File(fileName).text = ""
        
        def exec = { prefecture ->
            
            println "Let's go to $prefecture"
            driver.get("http://www.komeda.co.jp/search/shoplist.php?mod=search_by_select&from_search=1&s_pref=$prefecture")

            def lines = "" 
            
            boolean toContinue = true
            
            while(toContinue){
                def storeElements = driver.findElementsByClassName("clickable")
                println "${storeElements.size()} stores found."

                storeElements.collect{ WebElement elm ->
                    def storeName = elm.findElement(By.className("pb0")).findElement(By.tagName("a")).text;
                    def storeAddress = elm.findElements(By.className("d-inline-b"))[1].text;
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

                def goNextElms = driver.findElementsByClassName("paging_next")
                if(goNextElms){
                    goNextElms[0].click()
                }else{
                    toContinue = false
                }
            }
        }

//        (1..47).each(exec)

//        ClassLoader classLoader = Komeda.getClass().getClassLoader()
//        def prefectures = new File(classLoader.getResource("prefectures.txt").getFile()).text
        def prefectures = new File("src/main/resources/prefectures.txt").text
        println prefectures

        prefectures.split("\n").each(exec)
        
        driver.close();
            
    }
}
