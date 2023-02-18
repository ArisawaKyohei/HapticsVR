/*
* Menu.java
*
* Jul. 2011 by Muroran Institute of Technology
*/
package jp.ac.muroran_it.csse.simple_viewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
/**
*
画面に表示するメニュー。
*/
public class Menu {
    
    /**
    名前
    */
    private final String menuName;
    /**
    親メニュー
    */  
    private Menu superMenu = null;
    /**
    前のメニュー
    */
    private Menu previousMenu = null;
    /**
    次のメニュー
    */
    private Menu nextMenu = null;
    /**
    子メニュー
    */
    private Menu[] subMenu = new Menu[0];
    /**
    選択されている場合は
    true */
    private boolean isSelected = false;
    /**
    メニューアクション
    */
    private ActionListener menuAction = null;
    /**
    *
    メニューの名前を指定するコンストラクタ。
    *@param menuName
    メニューの名前。
    */
    private Menu(String menuName){
    this.menuName = menuName;
    }
    /**
    *
    メニューの生成。
    * @param state
    指定する状態。
    * @return
    生成したメニュー。
    */
    public static Menu create(final SimpleVRSpaceState state){
        Menu rootMenu = new Menu("rootMenu");
        rootMenu.isSelected = true;
        Menu menu1 = new Menu("menu1");
        Menu menu2 = new Menu("menu2");
        Menu subMenu21 = new Menu("subMenu21");
        subMenu21.menuAction = new ActionListener(){
            /**
             *
             ティーポットの色を青に変更。
             * @param e アクションイベント。
             */
             @Override
        public void actionPerformed(ActionEvent e) {
            //ティーポットを青にする
            state.setTeapotColor(new float[]{0.0f, 0.0f, 1.0f, 1.0f});
            }
        };
        Menu subMenu22 = new Menu("subMenu22");
        subMenu22.menuAction = new ActionListener(){
            /** 
            *
            ティーポットの色を赤に変更。
            * @param e アクションイベント。
            */
            @Override
            public void actionPerformed(ActionEvent e) {
            //ティーポットを赤にする
            state.setTeapotColor(new float[]{1.0f, 0.0f, 0.0f, 1.0f});
            }
        };
        
        rootMenu.addMenu(menu1);
        rootMenu.addMenu(menu2);
        menu2.addMenu(subMenu21);
        menu2.addMenu(subMenu22);
        
        return rootMenu;
    }
    
    /**
    *
    メニューの追加。
    * @param subMenu 追加するサブメニュー。
    */
    private void addMenu(Menu subMenu){
        Menu[] newSubMenu = new Menu[this.subMenu.length + 1];
        for (int i = 0; i < this.subMenu.length; ++i){
            newSubMenu[i] = this.subMenu[i];
        }
        newSubMenu[this.subMenu.length] = subMenu;
        
        newSubMenu[this.subMenu.length].superMenu = this;
        if (newSubMenu.length > 1){
            newSubMenu[newSubMenu.length - 2].nextMenu = newSubMenu[newSubMenu.length - 1];
            newSubMenu[newSubMenu.length - 1].previousMenu = newSubMenu[newSubMenu.length - 2];
        }
        this.subMenu = newSubMenu;
    }
    
    /**
    *
    選択されているメニューの取得。
    * @return
    選択されているもっとも枝葉のメニュー。
    */
    public Menu getSelectedMenu() {
        Menu[] subMenus = this.getSubMenus();
        for (int i = 0; i < subMenus.length; ++i) {
            if (subMenus[i].isSelected) {
                return subMenus[i].getSelectedMenu();
            }
        }
        return this;
    }
    
    /**
    *
    前のメニューに移動。
    * @param menu 移動前のメニュー。
    */
    public static void goPreviousMenu(Menu menu) {
        if (menu.previousMenu != null) {
            menu.isSelected = false;
            menu.previousMenu.isSelected = true;
        }
    }
    
    /**
    *
    次のメニューに移動。
    * @param menu 移動前のメニュー。
    */
    public static void goNextMenu(Menu menu) {
        if (menu.nextMenu != null) {
            menu.isSelected = false;
            menu.nextMenu.isSelected = true;
        }
    }
    /**
    *
    子メニューに移動。
    * @param menu 移動前のメニュー。
    */
    public static void goSubMenu(Menu menu) {
       if (menu.getSubMenus().length != 0) {
           menu.getSubMenus()[0].isSelected = true;
        }
    }
    /**
    *
    親メニューに移動。
    * @param menu 移動前のメニュー。
    */
    public static void goSuperMenu(Menu menu) {
        if (menu.superMenu != null) {
            menu.isSelected = false;
        }
    }
    /**
    *
    メニューの選択状態の取得。
    * @return
    メニューが選択されていた場合はtrue。
    */
    public boolean isSelected() {
        return isSelected;
    }
    /**
    *
    子メニューの取得。
    * @return 子メニュー。
    */
    public Menu[] getSubMenus() {
        return subMenu;
    }
    /**
    *
    メニューの名前の取得。
    * @return
    メニューの名前。
    */
    public String getMenuName(){
        return menuName;
    }
    /**
    *
    メニューに登録してあるアクションを実行。
    */
    public void doMenuAction(){
        if (menuAction != null){
            menuAction.actionPerformed(null);
            }
        }
    }