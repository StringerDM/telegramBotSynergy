package utils;

import functions.FilterOperation;
import functions.ImageOperation;

import java.awt.image.BufferedImage;

public class PhotoMessageUtils {
    static ImageOperation operation = FilterOperation::greyScale;

    public static void processingImage(String fileName) throws Exception {
        final BufferedImage image = ImageUtils.getImage(fileName);
        final RgbMaster rgbMaster = new RgbMaster(image);
        rgbMaster.changeImage(operation);
        ImageUtils.saveImage(rgbMaster.getImage(), fileName);
    }

    public static void setOperation(String filterName) {
        String filter = filterName.split(" ")[1];
        switch (filter) {
            case "greyScale" -> operation = FilterOperation::greyScale;
            case "onlyRed" -> operation = FilterOperation::onlyRed;
            case "onlyGreen" -> operation = FilterOperation::onlyGreen;
            case "onlyBlue" -> operation = FilterOperation::onlyBlue;
            case "sepia" -> operation = FilterOperation::sepia;
        }
    }
}
