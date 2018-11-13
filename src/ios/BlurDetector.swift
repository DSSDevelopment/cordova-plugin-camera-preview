import UIKit

extension UIImage {
    public func getPixelColor(pos: CGPoint) -> UIColor {
        let pixelData = self.cgImage!.dataProvider!.data
        let data: UnsafePointer<UInt8> = CFDataGetBytePtr(pixelData)
        
        let pixelInfo: Int = ((Int(self.size.width) * Int(pos.y)) + Int(pos.x)) * 4
        
        let r = CGFloat(data[pixelInfo]) / CGFloat(255.0)
        let g = CGFloat(data[pixelInfo+1]) / CGFloat(255.0)
        let b = CGFloat(data[pixelInfo+2]) / CGFloat(255.0)
        let a = CGFloat(data[pixelInfo+3]) / CGFloat(255.0)
        return UIColor(red: r, green: g, blue: b, alpha: a)
    }
}

extension UIColor {
    public func rgbComponents() -> (CGFloat, CGFloat, CGFloat) {
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        var alpha: CGFloat = 0
        getRed(&red, green: &green, blue: &blue, alpha: &alpha)
        return (red, green, blue)
    }
}

@objc(BlurDetector)
class BlurDetector: NSObject {
    let gridSize: CGFloat = 10
    
    public func detectBlur(image: UIImage) -> Float {
      var processedImage = image
      var maxContrast: CGFloat = 0.0
      var maxContrastLocation = CGPoint(x: 0, y: 0)

      if image.size.width > 1000 || image.size.height > 1000 {
        let scaleFactor = 1000 / image.size.width > image.size.height ? image.size.width : image.size.height
        let newSize = CGSize(width: image.size.width * scaleFactor, height: image.size.height * scaleFactor)
        UIGraphicsBeginImageContext(newSize)
        image.draw(in: CGRect(x: 0, y: 0, width: newSize.width, height: newSize.height))
        if let newImage = UIGraphicsGetImageFromCurrentImageContext() {
          processedImage = newImage
        }
        UIGraphicsEndImageContext()
      }
      
      for i in stride(from: gridSize / 2, to: processedImage.size.width - (gridSize / 2), by: gridSize) {
          var brightness = perceptualBrightness(image: processedImage, point: CGPoint(x: i, y: gridSize / 2))
          for j in stride(from: (gridSize / 2) + 1.0, to: processedImage.size.height - (gridSize / 2), by: 1) {
              let comparativeBrightness = perceptualBrightness(image: processedImage, point: CGPoint(x: i, y: j))
              if maxContrast < abs(comparativeBrightness - brightness) {
                  maxContrast = abs(comparativeBrightness - brightness)
                  maxContrastLocation = CGPoint(x: i, y: j)
              }
              brightness = comparativeBrightness
          }
      }
      
      for i2 in stride(from: gridSize / 2, to: processedImage.size.height - (gridSize / 2), by: gridSize) {
          var brightness = perceptualBrightness(image: processedImage, point: CGPoint(x: gridSize / 2, y: i2))
          for j2 in stride(from: (gridSize / 2) + 1, to: processedImage.size.width - (gridSize / 2), by: 1) {
              let comparativeBrightness = perceptualBrightness(image: processedImage, point: CGPoint(x: j2, y: i2))
              if maxContrast < abs(comparativeBrightness - brightness) {
                  maxContrast = abs(comparativeBrightness - brightness)
                  maxContrastLocation = CGPoint(x: j2, y: i2)
              }
              brightness = comparativeBrightness
          }
      }
      
      var maxV: CGFloat = 0.0
      var minV: CGFloat = 255.0
      for x in Int(maxContrastLocation.x - 4)...Int(maxContrastLocation.x + 4) {
          for y in Int(maxContrastLocation.y - 4)...Int(maxContrastLocation.y + 4) {
              let brightness = perceptualBrightness(image: processedImage, point: CGPoint(x: x, y: y))
              if brightness > maxV {
                  maxV = brightness
              }
              if brightness < minV {
                  minV = brightness
              }
          }
      }
      
      let sharpness = (maxContrast / (15 + maxV - minV)) * 27000.0 / 255.0
      return Float(sharpness)
    }
    
    private func perceptualBrightness(image: UIImage, point: CGPoint) -> CGFloat {
        let colorComponents = image.getPixelColor(pos: point).rgbComponents()
        let perceptualBrightness: CGFloat = floor(0.35 * (colorComponents.0 * 255.0) + 0.5 * (colorComponents.1 * 255.0) + 0.15 * (colorComponents.2 * 255.0))
        return perceptualBrightness
    }
}