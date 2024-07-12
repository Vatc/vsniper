module me.vatc.kokscraftstats {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;

    requires org.kordamp.bootstrapfx.core;
    requires org.yaml.snakeyaml;

    opens me.vatc.kokscraftstats to javafx.fxml;
    exports me.vatc.kokscraftstats;
}