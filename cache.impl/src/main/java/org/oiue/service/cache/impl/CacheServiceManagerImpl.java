package org.oiue.service.cache.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.oiue.service.cache.CacheService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.cache.Type;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "serial", "rawtypes" })
public class CacheServiceManagerImpl implements CacheServiceManager, Serializable {
	private Logger logger;
	private String cache_type = "cacheType";
	private String cache_default = "buffer";
	
	private Map<String, CacheService> caches = new HashMap<>();
	
	public CacheServiceManagerImpl(LogService logService) {
		logger = logService.getLogger(getClass());
	}
	
	public void updated(Map<String, ?> props) {
		String cache_type = props.get("cacheType") + "";
		if (!StringUtil.isEmptys(cache_type)) {
			this.cache_type = cache_type;
		}
		String cache_default = props.get("localCache") + "";
		if (!StringUtil.isEmptys(cache_default)) {
			this.cache_default = cache_default;
		}
	}
	
	@Override
	public void put(String name, Object object, Type type) {
		Map data = null;
		if (object instanceof Map) {
			data = (Map) object;
			String cacheType = data.remove(cache_type) + "";
			if (StringUtil.isEmptys(cacheType)) {
				cacheType = cache_default;
			}
			CacheService cache = caches.get(cacheType);
			if (cache == null) {
				String msg = "the key[" + cacheType + "] cache service not find!";
				logger.error(msg + ":" + data);
				throw new RuntimeException(msg);
			}
			cache.put(name, object, type);
		} else {
			// String msg = "the CacheServiceManager only cache Map args!";
			// logger.error(msg + ":" + object);
			// throw new RuntimeException(msg);
			CacheService cache = caches.get(cache_default);
			if (cache == null) {
				String msg = "the key[" + cache_default + "] cache service not find!";
				logger.error(msg + ":" + data);
				throw new RuntimeException(msg);
			}
			cache.put(name, object, type);
		}
	}
	
	@Override
	public void put(String name, String key, Object object, Type type) {
		Map data = null;
		if (object instanceof Map) {
			data = (Map) object;
			String cacheType = data.remove(cache_type) + "";
			if (StringUtil.isEmptys(cacheType)) {
				cacheType = cache_default;
			}
			CacheService cache = caches.get(cacheType);
			if (cache == null) {
				String msg = "the key[" + cacheType + "] cache service not find!";
				logger.error(msg + ":" + data);
				throw new RuntimeException(msg);
			}
			cache.put(name, key, object, type);
		} else {
			// String msg = "the CacheServiceManager only cache Map args!";
			// logger.error(msg + ":" + object);
			// throw new RuntimeException(msg);
			CacheService cache = caches.get(cache_default);
			if (cache == null) {
				String msg = "the key[" + cache_default + "] cache service not find!";
				logger.error(msg + ":" + data);
				throw new RuntimeException(msg);
			}
			cache.put(name, key, object, type);
		}
	}
	
	@Override
	public void put(String name, Object object, Type type, int expire) {
		Map data = null;
		if (object instanceof Map) {
			data = (Map) object;
			String cacheType = data.remove(cache_type) + "";
			if (StringUtil.isEmptys(cacheType)) {
				cacheType = cache_default;
			}
			CacheService cache = caches.get(cacheType);
			if (cache == null) {
				String msg = "the key[" + cacheType + "] cache service not find!";
				logger.error(msg + ":" + data);
				throw new RuntimeException(msg);
			}
			cache.put(name, object, type, expire);
		} else {
			// String msg = "the CacheServiceManager only cache Map args!";
			// logger.error(msg + ":" + object);
			// throw new RuntimeException(msg);
			CacheService cache = caches.get(cache_default);
			if (cache == null) {
				String msg = "the key[" + cache_default + "] cache service not find!";
				logger.error(msg + ":" + data);
				throw new RuntimeException(msg);
			}
			cache.put(name, object, type, expire);
		}
	}
	
	@Override
	public void put(String name, String key, Object object, Type type, int expire) {
		Map data = null;
		if (object instanceof Map) {
			data = (Map) object;
			String cacheType = data.remove(cache_type) + "";
			if (StringUtil.isEmptys(cacheType)) {
				cacheType = cache_default;
			}
			CacheService cache = caches.get(cacheType);
			if (cache == null) {
				String msg = "the key[" + cacheType + "] cache service not find!";
				logger.error(msg + ":" + data);
				throw new RuntimeException(msg);
			}
			cache.put(name, key, object, type, expire);
		} else {
			// String msg = "the CacheServiceManager only cache Map args!";
			// logger.error(msg + ":" + object);
			// throw new RuntimeException(msg);
			CacheService cache = caches.get(cache_default);
			if (cache == null) {
				String msg = "the key[" + cache_default + "] cache service not find!";
				logger.error(msg + ":" + data);
				throw new RuntimeException(msg);
			}
			cache.put(name, key, object, type, expire);
		}
	}
	
	@Override
	public Object get(String name) {
		return getCacheService().get(name);
	}
	
	@Override
	public Object get(String name, String key) {
		return getCacheService().get(name, key);
	}
	
	@Override
	public long delete(String name) {
		return getCacheService().delete(name);
	}
	
	@Override
	public long delete(String name, String... keys) {
		return getCacheService().delete(name, keys);
	}
	
	@Override
	public boolean exists(String name) {
		return getCacheService().exists(name);
	}
	
	private CacheService getCacheService() {
		CacheService cache = caches.get(cache_default);
		if (cache == null) {
			String msg = "the key[" + cache_default + "] cache service not find!";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		return cache;
	}
	
	@Override
	public boolean registerCacheService(String name, CacheService cache) {
		if (caches.containsKey(name)) {
			return false;
		} else {
			caches.put(name, cache);
		}
		return true;
	}
	
	@Override
	public boolean unRegisterCacheService(String name) {
		logger.info("unRegister CacheService :{} ", name);
		if (caches.containsKey(name)) {
			caches.remove(name);
			return true;
		}
		return false;
	}
	
	@Override
	public CacheService getCacheService(String name) {
		return StringUtil.isEmptys(name) ? caches.get(cache_default) : caches.get(name);
	}
	
	@Override
	public boolean contains(String name, String... keys) {
		return getCacheService().contains(name, keys);
	}
	
	@Override
	public void put(String name, String key, Type type, Object... objects) {
		
	}
	
	@Override
	public void swap(String nameA, String nameB) {
		
	}
}