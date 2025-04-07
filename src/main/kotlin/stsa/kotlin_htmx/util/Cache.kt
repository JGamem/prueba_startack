package stsa.kotlin_htmx.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant

interface SearchCache<K, V> {
    suspend fun get(key: K): V?
    suspend fun put(key: K, value: V)
    suspend fun invalidate(key: K)
    suspend fun invalidateAll()
}

class InMemorySearchCache<K, V>(
    private val expirationTimeMillis: Long = Duration.ofMinutes(10).toMillis()
) : SearchCache<K, V> {
    private val cache = mutableMapOf<K, CacheEntry<V>>()
    private val mutex = Mutex()
    
    override suspend fun get(key: K): V? = mutex.withLock {
        val entry = cache[key]
        if (entry != null && !entry.isExpired()) {
            entry.value
        } else {
            if (entry != null) {
                cache.remove(key)
            }
            null
        }
    }
    
    override suspend fun put(key: K, value: V) = mutex.withLock {
        cache[key] = CacheEntry(value, Instant.now().plusMillis(expirationTimeMillis))
    }
    
    override suspend fun invalidate(key: K): Unit = mutex.withLock {
        cache.remove(key)
    }
    
    override suspend fun invalidateAll(): Unit = mutex.withLock {
        cache.clear()
    }
    
    private data class CacheEntry<V>(
        val value: V,
        val expiresAt: Instant
    ) {
        fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)
    }
}