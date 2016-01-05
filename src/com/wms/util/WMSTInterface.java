/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wms.util;

import java.awt.Color;

/**
 *
 * @author mstemen
 */
public interface WMSTInterface {

        public static enum WMSColor
    {
        BoundingBox,
        WMSText,
        WMSTextAlt1,
        SelectedCell,
        CollectError,
        ValidateError,
        CollectValidated,

        ;

        static Color boundingBoxColor = new Color ( 189, 206, 222 );
        static Color WMSTextColor = new Color ( 10 , 10 ,10 );
        static Color WMSTextColorAlt1 = new Color ( 0, 102, 255 );
        static Color selectedCellColor = new Color ( 155,155,155 );
        static Color collectError = new Color ( 255, 153, 0 );
        static Color validateError = new Color ( 255, 153, 0 );
        static Color collectValidate = new Color ( 10, 10, 10 );


        public Color ranColor()
        {
            Color ranColorRet = null;



            return ranColorRet;
        }

        public Color toColor ()
        {
            return getColor (this);
        }
        public static Color getColor ( WMSColor color )
        {
            Color retColor = null;
            switch(color)
            {
                case BoundingBox:
                    retColor = boundingBoxColor;
                    break;
                case WMSText:
                    retColor = WMSTextColor;
                    break;
                case WMSTextAlt1:
                    retColor = WMSTextColorAlt1;
                    break;
                case SelectedCell:
                    retColor = selectedCellColor;
                    break;
                case CollectError:
                    retColor = collectError;
                    break;
                case ValidateError:
                    retColor = validateError;
                    break;
                case CollectValidated:
                    retColor = collectValidate;
                    break;
            }
            return retColor;
        }
    }
}
