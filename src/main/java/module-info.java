module me.vatc.kokscraftstats {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens me.vatc.kokscraftstats to javafx.fxml;
    exports me.vatc.kokscraftstats;
}