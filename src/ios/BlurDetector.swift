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

    public func getPixelGrayscaleValue(pos: CGPoint) -> CGFloat {
      var grayscale: CGFloat = 0.0
      var alpha: CGFloat = 0.0
     self.getPixelColor(pos: pos).getWhite(&grayscale, alpha: &alpha)
     return grayscale
    }

    var mono: UIImage? {
      let context = CIContext(options: nil)
      guard let currentFilter = CIFilter(name: "CIPhotoEffectMono") else { return nil }
      currentFilter.setValue(CIImage(image: self), forKey: kCIInputImageKey)
      if let output = currentFilter.outputImage, let cgImage = context.createCGImage(output, from: output.extent) {
        return UIImage(cgImage: cgImage, scale: scale, orientation: imageOrientation)
      }
      return nil
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
    private let gridSize: CGFloat = 50
    private let lapMatrix: [[CGFloat]] = [[0, 1, 0], [1, -4, 1], [0, 1, 0]]
    
    public func detectBlur(image: UIImage, completion: @escaping (_: Float) -> ()) {
      let processedImage = image
      if let monoImage = processedImage.mono {
        laplacian(image: monoImage) { (variance) in
            print("overall variance: \(variance)")
            completion(Float(variance))
        }
      } else {
        completion(0.0)
      }
    }

    private func laplacian(image: UIImage, completion: @escaping  (_: CGFloat) -> ()) {
      let pixelData = image.cgImage!.dataProvider!.data
      let data: UnsafePointer<UInt8> = CFDataGetBytePtr(pixelData)
        
      let tileMatrixEdgeSize = 3
      let tileEdgeSize = 100 * tileMatrixEdgeSize > Int(image.size.width) ? Int(image.size.width) / tileMatrixEdgeSize : 100
      let tileFormationSize = Int(image.size.width) < Int(image.size.height) ? Int(image.size.width * 0.2) : Int(image.size.height * 0.2)
      let tileOriginStep = tileFormationSize / tileMatrixEdgeSize
      let tileOriginX = (Int(image.size.width) / 2) - ((tileMatrixEdgeSize * tileOriginStep) / 2)
      let tileOriginY = (Int(image.size.height) / 2) - ((tileMatrixEdgeSize * tileOriginStep) / 2)
      var tiles = [CGRect]()
      for i in 0..<tileMatrixEdgeSize {
        for j in 0..<tileMatrixEdgeSize {
          tiles.append(CGRect(x: (i * tileOriginStep) + tileOriginX, y: (j * tileOriginStep) + tileOriginY, width: tileEdgeSize, height: tileEdgeSize))
        }
      }

      var tileResults = [CGFloat]()
      let tilingQueue = DispatchQueue(label: "com.dogesystemstudios.gcd.laplacian", attributes: [DispatchQueue.Attributes.concurrent], target: .global())
      let callbackQueue = DispatchQueue(label: "com.dogesystemstudios.gcd.callback", attributes: [])
      let tilingGroup = DispatchGroup()
      for tile in tiles {
        tilingGroup.enter()
        tilingQueue.async(group: tilingGroup) {
          self.convolveSingleTile(image: image, rawImageData: data, tile: tile, completion: { tileVariance in
            callbackQueue.sync() {
              tileResults.append(tileVariance)
            }
          tilingGroup.leave()
          })
        }
      }
      tilingGroup.notify(queue: .main) {
          print("Done convoluting tiles.")
          let averageVariance = tileResults.reduce(0.0, {x, y in x + y}) / CGFloat(tileResults.count)
          completion(averageVariance)
      }
    }

    private func convolveSingleTile(image: UIImage, rawImageData: UnsafePointer<UInt8>, tile: CGRect, completion: (_: CGFloat) -> ()) {
      var sum: CGFloat = 0.0
      var values = [CGFloat]()
      var r: CGFloat = 0.0
      var g: CGFloat = 0.0
      var b: CGFloat = 0.0
      var pos: CGPoint = CGPoint.zero
      var pixelInfo = 0

      for x in Int(tile.origin.x)..<Int(tile.origin.x + tile.size.width) {
        for y in Int(tile.origin.y)..<Int(tile.origin.y + tile.size.height) {
          let rejectEdgePixel = x - 1 < 0 || x + 2 > Int(image.size.width) || y - 1 < 0 || y + 2 > Int(image.size.height)
          if (!rejectEdgePixel) { 
            var accumulator: CGFloat = 0.0
            for i in 0..<3 {
              for j in 0..<3 {
                pos = CGPoint(x: x+i-1, y: y+j-1)
                pixelInfo = ((Int(image.size.width) * Int(pos.y)) + Int(pos.x)) * 4
                r = CGFloat(rawImageData[pixelInfo])
                g = CGFloat(rawImageData[pixelInfo+1])
                b = CGFloat(rawImageData[pixelInfo+2])
                let grayscale: CGFloat = floor(0.35 * r + 0.5 * g + 0.15 * b)
                accumulator += grayscale * lapMatrix[i][j]
              }
            }
            sum += accumulator
            values.append(accumulator)
          }
        }
      }
      let average: Float = Float(sum == 0 ? 1 : sum) / Float(values.count)
      let varianceSum = values.reduce(CGFloat(0.0), {acc, val in acc + pow((val - CGFloat(average)), 2)})
      let variance = varianceSum / CGFloat(values.count)
      completion(variance)
    }
}
