package io.quartic.tracker

import com.google.cloud.datastore.*
import com.google.cloud.datastore.StructuredQuery.CompositeFilter
import com.google.cloud.datastore.StructuredQuery.PropertyFilter
import io.quartic.common.logging.logger
import io.quartic.tracker.model.RegisteredUser
import io.quartic.tracker.model.UnregisteredUser
import io.quartic.tracker.model.User
import io.quartic.tracker.api.UserId
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

// For now, all of the methods will just propagate a DatastoreException if they fail
class UserDirectory(private val datastore: Datastore) {
    private val LOG by logger()

    private val random = Random()

    // We store everything under a single ancestor to avoid eventual-consistency weirdness.
    // I/O requirements are likely low, so should be ok for now.
    private val ancestorKey = datastore.newKeyFactory()
            .setKind(ANCESTOR_KIND)
            .newKey(ANCESTOR_NAME)
    private val keyFactory = datastore.newKeyFactory()
            .addAncestor(PathElement.of(ANCESTOR_KIND, ANCESTOR_NAME))
            .setKind(KIND)

    constructor(projectId: String, namespace: String) : this(DatastoreOptions.newBuilder()
            .setProjectId(projectId)
            .setNamespace(namespace)
            .build()
            .service)

    fun createUser(): UserId {
        val entity = datastore.put(Entity.newBuilder(keyFactory.newKey())
                .set(REGISTERED, false)
                .set(CODE, generateCode())
                .build())
        return UserId(entity.key.id)
    }

    fun deleteUser(userId: UserId): Boolean {
        return asTransaction {
            val key = keyFactory.newKey(userId.uid)
            if (get(key) == null) {
                false
            } else {
                delete(key)
                true
            }
        }
    }

    fun getUsers(): Map<UserId, User> {
        val query = Query.newEntityQueryBuilder()
                .setKind(KIND)
                .setFilter(PropertyFilter.hasAncestor(ancestorKey))
                .build()

        return datastore.run(query).asSequence()
                .map { it -> deserializeFrom(it) }
                .associateBy { it.id }
    }

    fun getUser(userId: UserId): User? {
        val entity = datastore.get(keyFactory.newKey(userId.uid))
        if (entity == null) {
            return null
        }
        return deserializeFrom(entity)
    }

    fun registerUser(code: String, publicKey: PublicKey): UserId? {
        return asTransaction {
            val results = run(Query.newKeyQueryBuilder()
                    .setKind(KIND)
                    .setFilter(CompositeFilter.and(
                            PropertyFilter.hasAncestor(ancestorKey),
                            PropertyFilter.eq(CODE, code)
                    ))
                    .build())

            if (!results.hasNext()) {
                null
            } else {
                val key = results.next()


                put(Entity.newBuilder(key)
                        .set(REGISTERED, true)
                        .set(PUBLIC_KEY, BlobValue.newBuilder(Blob.copyFrom(publicKey.encoded)).setExcludeFromIndexes(true).build())
                        .set(PUBLIC_KEY_ALGORITHM, StringValue.newBuilder(publicKey.algorithm).setExcludeFromIndexes(true).build())
                        .build())

                UserId(key.id)
            }
        }
    }

    private fun <R> asTransaction(block: Transaction.() -> R): R {
        val txn = datastore.newTransaction()
        try {
            val ret = txn.block()
            txn.commit()
            return ret
        } finally {
            if (txn.isActive) {
                txn.rollback()
            }
        }
    }

    private fun deserializeFrom(entity: Entity) = if (entity.getBoolean(REGISTERED)) {
        RegisteredUser(
                UserId(entity.key.id),
                KeyFactory.getInstance(entity.getString(PUBLIC_KEY_ALGORITHM))
                        .generatePublic(X509EncodedKeySpec(entity.getBlob(PUBLIC_KEY).toByteArray()))
        )
    } else {
        UnregisteredUser(
                UserId(entity.key.id),
                entity.getString(CODE)
        )
    }

    // TODO: this won't guarantee uniqueness
    private fun generateCode() = (random.nextInt(9000) + 1000).toString()

    companion object {
        private val ANCESTOR_KIND = "UserList"
        private val ANCESTOR_NAME = "default"
        private val KIND = "User"

        private val REGISTERED = "registered"
        private val CODE = "code"
        private val PUBLIC_KEY = "publicKey"
        private val PUBLIC_KEY_ALGORITHM = "publicKeyAlgorithm"
    }
}