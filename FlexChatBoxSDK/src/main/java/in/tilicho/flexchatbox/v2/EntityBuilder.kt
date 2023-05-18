package `in`.tilicho.flexchatbox.v2
import androidx.compose.runtime.Composable

class EntityBuilder {
    companion object {
        fun flexCameraEntity(successCallback: CameraSuccessCallback): Entity<AnyView> {
            return Entity(type = FlexInputType.CAMERA) {
                InvocationViewFactory.cameraInvocationCallback(successCallback)
            }
            /*val x: Entity<@Composable () -> Unit>*/

            /*
            val entity = Entity(type = FlexInputType.CAMERA) {
                // Content of the entity
                // You can compose your view here using Compose functions
            }
            */
        }

        fun flexGalleryEntity(successCallback: GallerySuccessCallback): Entity<AnyView> {
            return Entity<AnyView>(type = FlexInputType.GALLERY) {
                InvocationViewFactory.galleryInvocationCallback(successCallback)
            }
        }

        fun flexMicEntity(): Entity<AnyView> {
            return Entity<AnyView>(type = FlexInputType.MIC) {
                InvocationViewFactory.microphoneInvocationCallback()
            }
        }

        fun <Content : @Composable () -> Unit> flexCustomEntity(invocationContentView: Content): Entity<AnyView> {
            return Entity<AnyView>(type = FlexInputType.CUSTOM) {
                invocationContentView.invoke()//.eraseToAnyView()
            }
        }
    }



    /*
    * Below is not in iOS, own implementation
    * */

    private var type: FlexInputType = FlexInputType.TEXT
    private var invocationContentView: (@Composable () -> Unit)? = null

    fun setType(type: FlexInputType): EntityBuilder {
        this.type = type
        return this
    }

    fun setInvocationContentView(content: @Composable () -> Unit): EntityBuilder {
        this.invocationContentView = content
        return this
    }

    fun build(): Entity<(@Composable () -> Unit)> {
        requireNotNull(invocationContentView) { "Invocation content view must be set." }
        return Entity(type, invocationContentView!!)
    }


    /*
    * usage
    val entity = EntityBuilder()
    .setType(FlexInputType.CAMERA)
    .setInvocationContentView {
        // Content of the entity
        // You can compose your view here using Compose functions
    }
    .build()
    * */
}
