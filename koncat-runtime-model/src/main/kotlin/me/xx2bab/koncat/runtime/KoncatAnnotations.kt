package me.xx2bab.koncat.runtime

/**
 * Used for triggering extension processors.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class KoncatExtend(val metaDataInJson: String)

/**
 * Used for each module's export file as index.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY)
annotation class KoncatMeta(val metaDataInJson: String)


