package game;

import java.util.ArrayList;

import physics.BMWM3Properties;
import physics.CarProperties;
import physics.DodgeViperProperties;
import physics.SkylineProperties;
import physics.TypeCarProperties;
import save.Comptes;
import save.ProfilCurrent;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;

import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.ImageSelect;
import de.lessvoid.nifty.controls.ImageSelectSelectionChangedEvent;
import de.lessvoid.nifty.controls.TextField;

public class Achat extends AbstractScreenController {
	
	private InputManager inputManager;
	
	private final String IMGCAR = "imagecar";
	
	private ArrayList<CarProperties> dataAllCar;
	private ArrayList<CarProperties> allCarJoueur;
	
	private TextField monnaie;
	private ArrayList<Integer> tabprix;
	private int calcul;
	
	private TextField typeCar;
	private TextField weight;
	private TextField puis;
	private TextField nitro;
	private TextField prix;
	private TextField msgerr;
	private String hasCar = "VOUS AVEZ DEJA CETTE VOITURE !";
	
	private ImageSelect imgCar;
	
	private final int DODGE = 0;
	private final int BMWM3 = 1;
	private final int FERRARI = 2;
	
	public Achat() {
		super();
		dataAllCar = Comptes.getListCar();
		tabprix = new ArrayList<Integer>();
		tabprix.add(44000);
		tabprix.add(68000);
		tabprix.add(86000);
		calcul = ProfilCurrent.getInstance().getMonnaie();
		allCarJoueur = ProfilCurrent.getInstance().getCar();
	}
	
	@Override
	public void initialize(AppStateManager stateManager, Application a) {
		super.initialize(stateManager, a);

		inputManager = app.getInputManager();
		inputManager.setCursorVisible(true);

		this.inputManager = app.getInputManager();
		
		imgCar = screen.findNiftyControl(IMGCAR, ImageSelect.class);
		typeCar = screen.findNiftyControl("typeCar", TextField.class);
		weight = screen.findNiftyControl("weight", TextField.class);
		puis  = screen.findNiftyControl("puis", TextField.class);
		nitro  = screen.findNiftyControl("nitro", TextField.class);
		
		prix = screen.findNiftyControl("prix", TextField.class);
		
		msgerr = screen.findNiftyControl("msgerr", TextField.class);
		
		typeCar.setText(TypeCarProperties.CORVETTE.toString());
		weight.setText(Integer.toString(dataAllCar.get(DODGE+1).getWeight()));
		puis.setText(Integer.toString(dataAllCar.get(DODGE+1).getPuissance()));
		prix.setText(Integer.toString(tabprix.get(DODGE)));
		nitro.setText((dataAllCar.get(DODGE+1).isImprovenitro()) ? "Oui" : "Non");
		for (int j = 0; j < allCarJoueur.size(); ++j) {
			if (allCarJoueur.get(j).getTypeCar().equals(TypeCarProperties.CORVETTE)) {
				msgerr.setText(hasCar);
			}
		}
		
		typeCar.setEnabled(false);
		prix.setEnabled(false);		
		weight.setEnabled(false);
		puis.setEnabled(false);
		nitro.setEnabled(false);
		msgerr.setEnabled(false);
		
		calcul = ProfilCurrent.getInstance().getMonnaie();
		monnaie = screen.findNiftyControl("monnaie", TextField.class);
		monnaie.setText(Integer.toString(calcul));
		monnaie.setEnabled(false);
		
	}
	
	@NiftyEventSubscriber(id=IMGCAR)
	public void onChangePhoto(final String id, final ImageSelectSelectionChangedEvent event) {
		changeDataPhoto();
	}
	
	public void changePhoto() {
		int maxphoto = 2;
		if (imgCar.getSelectedImageIndex() == maxphoto) {
			imgCar.setSelectedImageIndex(1);
			imgCar.backClick();
		} else if (imgCar.getSelectedImageIndex() >= 0 && imgCar.getSelectedImageIndex() < maxphoto){
			imgCar.forwardClick();
		}
		
		changeDataPhoto();
	}
	
	public void changeDataPhoto() {
		
		typeCar.setText("");
		weight.setText("");
		puis.setText("");
		nitro.setText("");
		msgerr.setText("");
		
		if (imgCar.getSelectedImageIndex() == DODGE) {
			for (int i = 0; i < dataAllCar.size(); ++i) {
				if (dataAllCar.get(i).getTypeCar().equals(TypeCarProperties.CORVETTE)) {
					typeCar.setText(dataAllCar.get(i).getTypeCar().toString());
					puis.setText(Integer.toString(dataAllCar.get(i).getPuissance()));
					weight.setText(Integer.toString(dataAllCar.get(i).getWeight()));
					nitro.setText((dataAllCar.get(i).isImprovenitro()) ? "Oui" : "Non");
					prix.setText(Integer.toString(tabprix.get(DODGE)));
					for (int j = 0; j < allCarJoueur.size(); ++j) {
						if (allCarJoueur.get(j).getTypeCar().equals(TypeCarProperties.CORVETTE)) {
							msgerr.setText(hasCar);
						}
					}
					break;
				}						
			}
		} else if (imgCar.getSelectedImageIndex() == BMWM3) {
			for (int i = 0; i < dataAllCar.size(); ++i) {
				if (dataAllCar.get(i).getTypeCar().equals(TypeCarProperties.BMWM3)) {
					typeCar.setText(dataAllCar.get(i).getTypeCar().toString());
					puis.setText(Integer.toString(dataAllCar.get(i).getPuissance()));
					weight.setText(Integer.toString(dataAllCar.get(i).getWeight()));
					nitro.setText((dataAllCar.get(i).isImprovenitro()) ? "Oui" : "Non");
					prix.setText(Integer.toString(tabprix.get(BMWM3)));
					for (int j = 0; j < allCarJoueur.size(); ++j) {
						if (allCarJoueur.get(j).getTypeCar().equals(TypeCarProperties.BMWM3)) {
							msgerr.setText(hasCar);
						}
					}
					break;
				}						
			}
		} else {
			for (int i = 0; i < dataAllCar.size(); ++i) {
				if (dataAllCar.get(i).getTypeCar().equals(TypeCarProperties.FERRARI)) {
					typeCar.setText(dataAllCar.get(i).getTypeCar().toString());
					puis.setText(Integer.toString(dataAllCar.get(i).getPuissance()));
					weight.setText(Integer.toString(dataAllCar.get(i).getWeight()));
					nitro.setText((dataAllCar.get(i).isImprovenitro()) ? "Oui" : "Non");
					prix.setText(Integer.toString(tabprix.get(FERRARI)));
					for (int j = 0; j < allCarJoueur.size(); ++j) {
						if (allCarJoueur.get(j).getTypeCar().equals(TypeCarProperties.FERRARI)) {
							msgerr.setText(hasCar);
						}
					}
					break;
				}						
			} //for
		}
	}
	
	public void gotoChooseProfil() {
		app.gotoAffProfil();
	}
	
	public void Enregistrer() {
		String indic = "IMPOSSIBLE, VOUS AVEZ DEJA LA VOITURE !";
		if (!msgerr.getText().equals("")) {
			msgerr.setText(indic);
		} else {
			if (imgCar.getSelectedImageIndex() == BMWM3) {
				calcul -= tabprix.get(1);
			} else if (imgCar.getSelectedImageIndex() == DODGE) {
				calcul -= tabprix.get(DODGE);
			} else if (imgCar.getSelectedImageIndex() == FERRARI) {
				calcul -= tabprix.get(FERRARI);
			}
			
			if (calcul < 0) {
				calcul = ProfilCurrent.getInstance().getMonnaie();
				msgerr.setText("IMPOSSIBLE, VOUS N'AVEZ PAS ASSEZ D'ARGENT !");
			} else {
				if (imgCar.getSelectedImageIndex() == BMWM3) {
					for (int i = 0; i < dataAllCar.size(); ++i) {
						if (dataAllCar.get(i).getTypeCar().equals(TypeCarProperties.BMWM3)) {
							ProfilCurrent.getInstance().setChoixCar(allCarJoueur.size());
							allCarJoueur.add(dataAllCar.get(i));
							break;
						}
					}
				} else if (imgCar.getSelectedImageIndex() == DODGE) {
					for (int i = 0; i < dataAllCar.size(); ++i) {
						if (dataAllCar.get(i).getTypeCar().equals(TypeCarProperties.CORVETTE)) {
							ProfilCurrent.getInstance().setChoixCar(allCarJoueur.size());
							allCarJoueur.add(dataAllCar.get(i));
							break;
						}
					}
				} else if (imgCar.getSelectedImageIndex() == FERRARI) {
					for (int i = 0; i < dataAllCar.size(); ++i) {
						if (dataAllCar.get(i).getTypeCar().equals(TypeCarProperties.FERRARI)) {
							ProfilCurrent.getInstance().setChoixCar(allCarJoueur.size());
							allCarJoueur.add(dataAllCar.get(i));
							break;
						}
					}
				}
				ProfilCurrent.getInstance().setCar(allCarJoueur);
				ProfilCurrent.getInstance().setMonnaie(calcul);
				Comptes.modifier(ProfilCurrent.getInstance());
				app.gotoAffProfil();
			}		
		}
	}
}
