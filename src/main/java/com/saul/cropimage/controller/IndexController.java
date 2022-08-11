package com.saul.cropimage.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/")
public class IndexController {

	@GetMapping("/")
	public String inicio(Model modelo) {
		modelo.addAttribute("var", "s");
		return "index";
	}

	@PostMapping("recorta")
	public String recotar(Model modelo, @RequestParam(value = "imagen") MultipartFile imagen) {
		try {
			BufferedImage source = ImageIO.read(imagen.getInputStream());
			
			BufferedImage imagenRecortada = croppedImage(source, 0.2);
			imagenRecortada = resizeImage(imagenRecortada, 250, 80);

			
			ImageIO.write(source, "JPG", new File("D:\\tmp\\firma.jpg") );
			ImageIO.write(imagenRecortada, "PNG", new File("D:\\tmp\\firmaComprimida.png") );
			

			ByteArrayOutputStream outOriginal = new ByteArrayOutputStream();
			ImageIO.write(source, "PNG", outOriginal);
			
			ByteArrayOutputStream outComprimido = new ByteArrayOutputStream();
			ImageIO.write(imagenRecortada, "PNG", outComprimido);

			modelo.addAttribute("srcOriginal", "data:image/png;base64," + Base64.getEncoder().encodeToString(outOriginal.toByteArray()));
			modelo.addAttribute("srcComprimido", "data:image/png;base64," + Base64.getEncoder().encodeToString(outComprimido.toByteArray()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "resultado";
	}
	
	public void validaImagen(){}
	
	public BufferedImage resizeImage(BufferedImage image,Integer width, Integer heigth) {
		BufferedImage resizedImage = new BufferedImage(width, heigth, BufferedImage.TYPE_INT_RGB);
	    Graphics2D graphics2D = resizedImage.createGraphics();
	    graphics2D.drawImage(image, 0, 0, width, heigth, null);
	    graphics2D.dispose();
	    return resizedImage;
	}
	
	public BufferedImage croppedImage(BufferedImage image, double tolerance) {
		   // Get our top-left pixel color as our "baseline" for cropping
		   int baseColor = image.getRGB(0, 0);

		   int width = image.getWidth();
		   int height = image.getHeight();

		   int topY = Integer.MAX_VALUE, topX = Integer.MAX_VALUE;
		   int bottomY = -1, bottomX = -1;
		   for(int y=0; y<height; y++) {
		      for(int x=0; x<width; x++) {
		         if (colorWithinTolerance(baseColor, image.getRGB(x, y), tolerance)) {
		            if (x < topX) topX = x;
		            if (y < topY) topY = y;
		            if (x > bottomX) bottomX = x;
		            if (y > bottomY) bottomY = y;
		         }
		      }
		   }

		   BufferedImage destination = new BufferedImage( (bottomX-topX+1), 
		                 (bottomY-topY+1), BufferedImage.TYPE_INT_ARGB);

		   destination.getGraphics().drawImage(image, 0, 0, 
		               destination.getWidth(), destination.getHeight(), 
		               topX, topY, bottomX, bottomY, null);

		   return destination;
		}

	private boolean colorWithinTolerance(int a, int b, double tolerance) {
		int aAlpha = (int) ((a & 0xFF000000) >>> 24); // Alpha level
		int aRed = (int) ((a & 0x00FF0000) >>> 16); // Red level
		int aGreen = (int) ((a & 0x0000FF00) >>> 8); // Green level
		int aBlue = (int) (a & 0x000000FF); // Blue level

		int bAlpha = (int) ((b & 0xFF000000) >>> 24); // Alpha level
		int bRed = (int) ((b & 0x00FF0000) >>> 16); // Red level
		int bGreen = (int) ((b & 0x0000FF00) >>> 8); // Green level
		int bBlue = (int) (b & 0x000000FF); // Blue level

		double distance = Math.sqrt((aAlpha - bAlpha) * (aAlpha - bAlpha) + (aRed - bRed) * (aRed - bRed)
				+ (aGreen - bGreen) * (aGreen - bGreen) + (aBlue - bBlue) * (aBlue - bBlue));

		// 510.0 is the maximum distance between two colors
		// (0,0,0,0 -> 255,255,255,255)
		double percentAway = distance / 510.0d;

		return (percentAway > tolerance);
	}
	
	
	
}
