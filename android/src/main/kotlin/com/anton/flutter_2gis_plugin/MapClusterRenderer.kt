package com.anton.flutter_2gis_plugin

import ru.dgis.sdk.map.LogicalPixel
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.SimpleClusterObject
import ru.dgis.sdk.map.SimpleClusterOptions
import ru.dgis.sdk.map.SimpleClusterRenderer
import ru.dgis.sdk.map.SimpleMapObject
import ru.dgis.sdk.map.TextPlacement
import ru.dgis.sdk.map.TextStyle

class MapClusterRenderer : SimpleClusterRenderer {
    override fun renderCluster(cluster: SimpleClusterObject): SimpleClusterOptions {
        var topObject: SimpleMapObject = cluster.objects.first()
        cluster.objects.forEach { if (it.zIndex.value > topObject.zIndex.value) topObject = it }
        return SimpleClusterOptions(
            icon = (topObject as Marker).icon,
            text = cluster.objectCount.toString(),
            textStyle = TextStyle(
                fontName = "Roboto",
                fontSize = LogicalPixel(14.0f),
                strokeWidth = LogicalPixel(1.0f),
                textPlacement = TextPlacement.CENTER_CENTER,
            )
        );
    }
}