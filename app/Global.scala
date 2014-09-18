import play.api._
import play.api.cache._

import net.sf.ehcache._
import net.sf.ehcache.config._
import net.sf.ehcache.store._
import net.sf.ehcache.config.PersistenceConfiguration._


object Global extends GlobalSettings {
 override def onStart(app: Application) {
    Logger.info("Application has started")
    val cache = app.plugin[EhCachePlugin].get
    //Create a singleton CacheManager using defaults
    val manager = cache.manager
  	val testCache = new Cache(
  	  new CacheConfiguration("testCache", 50)
  	    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
  	    .eternal(false)
  	    .timeToLiveSeconds(3600)
  	    .timeToIdleSeconds(3600)
  	    .persistence(new PersistenceConfiguration()
  	    	.strategy(Strategy.LOCALTEMPSWAP))
        .maxEntriesLocalDisk(1000)
    )

	 manager.addCache(testCache)
  }
}