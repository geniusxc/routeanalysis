package xr.example.com.routeplan.station_info;

/**
 * Created by Administrator on 2018/6/19.
 */

 public class point{//是为了解决工程优先算法而设计的
    public double x,y;
    public Integer level;
    public String z;
    public point(double x,double y,Integer level,String z){//x是纬度，y是经度
        this.x=x;
        this.y=y;
        this.level=level;
        this.z=z;
    }
    public point (double x,double y,Integer i){
        this.x=x;
        this.y=y;
        this.level=i;
    }
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public Integer getLevel(){
        return level;
    }
    public String getZ(){return z;}

    public void setX(double x) {
        this.x=x;
    }
    public void setY(double y) {
        this.y=y;
    }
    public void setLevel(int level) {
        this.level=level;
    }
    public void setZ(String z){this.z=z;}

}
