package xr.example.com.routeplan.routeinfo;

/**
 * @author lixx
 * @brief
 * @createTime 2017/9/7
 */

public class Utils {
    /**
     * @brief 获取音频的时长(将时间由毫秒转化为分钟和秒来表示)
     * @param[in] duration 时间(毫秒) ruturn String 转化后的时间
     */
    public static String getAudioTime(int duration) {
        int hh = duration / 3600;
        int mm = duration % 3600 / 60;
        int ss = duration % 60;
        String strTemp = null;
        if (0 != hh) {
            strTemp = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            strTemp = String.format("%02d:%02d", mm, ss);
        }

        // return String.format("%02d:%02d:%02d", hh, mm, ss);
        return strTemp;
    }

}
