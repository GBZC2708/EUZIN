package pe.com.zzynan.euzin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import pe.com.zzynan.euzin.ui.navigation.EuzinNavHost
import pe.com.zzynan.euzin.ui.theme.EUZINTheme

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EUZINTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EuzinNavHost(container = appContainer)
                }
            }
        }
    }
}
